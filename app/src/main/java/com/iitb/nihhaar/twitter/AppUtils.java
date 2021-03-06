package com.iitb.nihhaar.twitter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Nihhaar on 10/4/2017.
 * This is class is for editing app wide properties.
 */

public class AppUtils {

    public static String servIP = "192.168.100.7";
    public static String webApp = "TwitterBackend";
    public static int servPort = 8080;

    public static final String LOGIN_PREFS_FILE = "login_prefs";
    public static String PAGE_SIZE = "10";

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    /* Clear shared prefs & redirect to Login */
    public static void logOut(final Context mContext){
        /* Remove shared preferences */
        SharedPreferences.Editor remLogin1 = mContext.getSharedPreferences(LOGIN_PREFS_FILE,MODE_PRIVATE).edit();
        remLogin1.clear();
        remLogin1.apply();

        /* Invalidate session in server if present */
        String url = "http://" + AppUtils.servIP + ":" + AppUtils.servPort + "/" + AppUtils.webApp + "/Logout";
        LogoutAsyncTask logoutAsyncTask = new LogoutAsyncTask(mContext, new MyInterface() {
            @Override
            public void myMethod(String result) {
                if(result.equals("0") || result.equals("1")){
                    Toast.makeText(mContext, "Unable to connect to Server", Toast.LENGTH_SHORT).show();
                }
                else{
                    Intent login = new Intent(mContext, LoginActivity.class);
                    login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mContext.startActivity(login);
                }
            }
        });
        logoutAsyncTask.execute(url);
    }

    /*
     * We are using this interface to generate a toast message on UI thread using the asynctask.
     * Note that this can also be done using the 'runOnUiThread' method.
     */
    interface MyInterface {
        void myMethod(String result);
    }

    /* LogoutAsyncTask - AsyncTask for login check */
    private static class LogoutAsyncTask extends AsyncTask<String, Void, String> {
        private String mTAG = "LogoutAsyncTask";
        private MyInterface mListener;
        private Context mContext;

        public LogoutAsyncTask(Context mContext, MyInterface mListener){
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
                    conn.connect();
                    result = "2";
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

    public static void addPostsToAdapter(Context context, String jsonResponse, RecyclerView.Adapter mAdapter){
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            Boolean status = jsonObject.getBoolean("status");
            if(status){
                JSONArray jsonArr = jsonObject.getJSONArray("data");
                for (int index = 0; index < jsonArr.length(); index++) {
                    JSONObject jobj = jsonArr.getJSONObject(index);

                    /* Post data */
                    Posts posts = new Posts();
                    posts.setPostUser(jobj.getString("uid"));
                    posts.setPostText(jobj.getString("text"));
                    posts.setPostid(jobj.getInt("postid"));
                    posts.setHasImage(jobj.getBoolean("hasimg"));

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
                AppUtils.logOut(context);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
