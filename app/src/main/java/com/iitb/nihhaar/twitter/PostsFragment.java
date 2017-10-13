package com.iitb.nihhaar.twitter;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private String url;
    private boolean isItMe;

    public PostsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        List<Posts> posts = new ArrayList<>();
        mAdapter = new MyAdapter(getContext(), posts);
        isItMe = getArguments().getBoolean("isItMe");

        if(isItMe)
            url = "http://" + AppUtils.servIP + ":" + AppUtils.servPort + "/" + AppUtils.webApp + "/SeeMyPosts";
        else
            url = "http://" + AppUtils.servIP + ":" + AppUtils.servPort + "/" + AppUtils.webApp + "/SeePosts";

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.my_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextDataFromApi(page);
            }
        };
        // Add the scroll listener and attach the adapter to RecyclerView
        mRecyclerView.addOnScrollListener(scrollListener);
        mRecyclerView.setAdapter(mAdapter);
        loadNextDataFromApi(0);
        scrollListener.resetState();
    }

    /* In our case assume page size is 10 */
    public void loadNextDataFromApi(int offset) {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
        PostFetch postFetch = new PostFetch(getContext(), new AppUtils.MyInterface() {
            @Override
            public void myMethod(String response) {
                if(response.equals("0")){
                    Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                }
                else if(response.equals("1")){
                    Toast.makeText(getContext(), "Unable to connect to server", Toast.LENGTH_SHORT).show();
                }
                else {
                    AppUtils.addPostsToAdapter(getContext(), response, mAdapter);
                }
            }
        });
        postFetch.execute(url, Integer.valueOf(offset*10).toString(), AppUtils.PAGE_SIZE);
    }
}
