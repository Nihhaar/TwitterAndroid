package com.iitb.nihhaar.twitter;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

public class UserFragment extends Fragment {

    private TextView mUserView;
    private TextView followBtn;
    private String name;
    private String uid;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        name = ((SearchSuggestions)getArguments().get("user")).getName();
        uid = ((SearchSuggestions)getArguments().get("user")).getUid();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mUserView = (TextView)view.findViewById(R.id.username);
        followBtn = (TextView)view.findViewById(R.id.follow);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);

        mUserView.setText(name);
        IsFollowTask isFollowTask = new IsFollowTask(getContext(), new AppUtils.MyInterface() {
            @Override
            public void myMethod(String result) {
                if(result.equals("0")){
                    Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                }
                else if(result.equals("1")){
                    Toast.makeText(getContext(), "Unable to connect to server", Toast.LENGTH_SHORT).show();
                }
                else {
                    try {
                        Log.d("Follow", result);
                        JSONObject jsonObject = new JSONObject(result);
                        Boolean status = jsonObject.getBoolean("status");
                        if (status) {
                            if (!jsonObject.getBoolean("data"))
                                followBtn.setText("Follow");
                            else
                                followBtn.setText("UnFollow");
                        }
                        else
                            AppUtils.logOut(getContext());
                    } catch (JSONException ex){
                        ex.printStackTrace();
                    }
                }
            }
        });
        String url1 = "http://" + AppUtils.servIP + ":" + AppUtils.servPort + "/" + AppUtils.webApp + "/IsFollow";
        isFollowTask.execute(url1, uid);

        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FollowAsyncTask followAsyncTask = new FollowAsyncTask(getContext(), new AppUtils.MyInterface() {
                    @Override
                    public void myMethod(String response) {
                        if(response.equals("0")){
                            Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                        }
                        else if(response.equals("1")){
                            Toast.makeText(getContext(), "Unable to connect to server", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            if(followBtn.getText().toString().equals("Follow")){
                                followBtn.setText("UnFollow");
                                Toast.makeText(getContext(), "Following user " + name, Toast.LENGTH_SHORT).show();
                            }
                            else {
                                followBtn.setText("Follow");
                                Toast.makeText(getContext(), "Unfollowed user " + name, Toast.LENGTH_SHORT).show();
                            }
                            }
                    }
                });
                String url2 = null;
                if(followBtn.getText().toString().equals("Follow"))
                    url2 = "http://" + AppUtils.servIP + ":" + AppUtils.servPort + "/" + AppUtils.webApp + "/Follow";
                else
                    url2 = "http://" + AppUtils.servIP + ":" + AppUtils.servPort + "/" + AppUtils.webApp + "/Unfollow";
                followAsyncTask.execute(url2, uid);
            }
        });

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
        String url = "http://" + AppUtils.servIP + ":" + AppUtils.servPort + "/" + AppUtils.webApp + "/SeeUserPosts";
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
        postFetch.execute(url, Integer.valueOf(offset).toString(), AppUtils.PAGE_SIZE, uid);
    }

    /* FollowAsyncTask - AsyncTask for following a user */
    private class FollowAsyncTask extends AsyncTask<String, Void, String> {
        private AppUtils.MyInterface mListener;
        private Context mContext;

        public FollowAsyncTask(Context mContext, AppUtils.MyInterface mListener){
            this.mContext = mContext;
            this.mListener = mListener;
        }

        @Override
        protected String doInBackground(String... args) {

            String result = "0";
            if(AppUtils.isNetworkAvailable(mContext)){
                try {
                    /* Authenticate with the server, if success store the credentials in shared prefs */
                    URLConnection conn = new URL(args[0]).openConnection();
                    conn.setDoOutput(true);

                    /* Create the post data */
                    String data = URLEncoder.encode("uid", "UTF-8")
                            + "=" + URLEncoder.encode(args[1], "UTF-8");

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

    /* IsFollowTask - AsyncTask for checking whether current user is following another */
    private class IsFollowTask extends AsyncTask<String, Void, String> {
        private String mTAG = "LoginAsyncTask";
        private AppUtils.MyInterface mListener;
        private Context mContext;

        public IsFollowTask(Context mContext, AppUtils.MyInterface mListener){
            this.mContext = mContext;
            this.mListener = mListener;
        }

        @Override
        protected String doInBackground(String... args) {

            String result = "0";
            if(AppUtils.isNetworkAvailable(mContext)){
                try {
                    /* Authenticate with the server, if success store the credentials in shared prefs */
                    URLConnection conn = new URL(args[0]).openConnection();
                    conn.setDoOutput(true);

                    /* Create the post data */
                    String data = URLEncoder.encode("uid", "UTF-8")
                            + "=" + URLEncoder.encode(args[1], "UTF-8");

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
