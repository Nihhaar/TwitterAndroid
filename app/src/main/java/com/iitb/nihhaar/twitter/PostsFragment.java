package com.iitb.nihhaar.twitter;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private int currentPostPosition;

    private static String TAG = "PostFragment";
    private static String PAGE_SIZE = "10";

    public PostsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.my_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        List<Posts> posts = new ArrayList<>();
        mAdapter = new MyAdapter(getContext(), posts);

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
    }

    /* In our case assume page size is 10 */
    public void loadNextDataFromApi(int offset) {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
        String url = "http://" + AppUtils.servIP + ":" + AppUtils.servPort + "/" + AppUtils.webApp + "/SeeMyPosts";
        PostFetch postFetch = new PostFetch(getContext(), new MyInterface() {
            @Override
            public void myMethod(String response) {
                if(response.equals("0")){
                    Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                }
                else if(response.equals("1")){
                    Toast.makeText(getContext(), "Unable to connect to server", Toast.LENGTH_SHORT).show();
                }
                else {
                    parseJsonData(response);
                }
            }
        });
        postFetch.execute(url, Integer.valueOf(offset).toString(), PAGE_SIZE);
    }

    private void parseJsonData(String jsonResponse){
        try {
            Log.d(TAG, jsonResponse);
            JSONObject jsonObject = new JSONObject(jsonResponse);
            Boolean status = jsonObject.getBoolean("status");
            if(status){
                JSONArray jsonArr = jsonObject.getJSONArray("data");
                Log.d(TAG, jsonArr.toString());
                for (int index = 0; index < jsonArr.length(); index++) {
                    JSONObject jobj = jsonArr.getJSONObject(index);

                    /* Post data */
                    Posts posts = new Posts();
                    posts.setPostUser(jobj.getString("uid"));
                    posts.setPostText(jobj.getString("text"));
                    posts.setPostid(jobj.getInt("postid"));

                    /* Comment data */
                    JSONArray cArr = jobj.getJSONArray("Comment");
                    ArrayList<Comments> comments = new ArrayList<>();
                    for(int i = 0; i < cArr.length(); i++) {
                        Comments comment = new Comments();
                        comment.setCommentor(cArr.getJSONObject(i).getString("uid"));
                        comment.setComment(cArr.getJSONObject(i).getString("text"));
                        comments.add(comment);
                    }
                    posts.setComments(comments);

                    ((MyAdapter)mAdapter).appendData(posts);
                    mAdapter.notifyDataSetChanged();
                }
            }
            else
                AppUtils.logOut(getContext());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    interface MyInterface {
        void myMethod(String result);
    }

    /* PostFetch - AsyncTask for fetching posts */
    private class PostFetch extends AsyncTask<String, Void, String> {
        private String mTAG = "PostFetch";
        private MyInterface mListener;
        private Context mContext;

        public PostFetch(Context mContext, MyInterface mListener){
            this.mContext = mContext;
            this.mListener = mListener;
        }

        @Override
        protected String doInBackground(String... args) {
            Log.d(mTAG, "Connecting to Server");
            String result = "0";
            if(AppUtils.isNetworkAvailable(mContext)){
                try {
                    /* Authenticate with the server, if success store the credentials in shared prefs */
                    URLConnection conn = new URL(args[0]).openConnection();
                    conn.setDoOutput(true);

                    /* Create the post data */
                    String data = URLEncoder.encode("offset", "UTF-8")
                            + "=" + URLEncoder.encode(args[1], "UTF-8");

                    data += "&" + URLEncoder.encode("limit", "UTF-8") + "="
                            + URLEncoder.encode(args[2], "UTF-8");

                    /* Post the data */
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                    writer.write(data);
                    writer.flush();
                    writer.close();

                    /* Get the response from server */
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    result = stringBuilder.toString();

                } catch (IOException ex){
                    result = "1";
                    ex.printStackTrace();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (mListener != null)
                mListener.myMethod(result);
        }
    }

}
