package com.iitb.nihhaar.twitter;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by Nihhaar on 10/12/2017.
 */

/* PostFetch - AsyncTask for fetching posts */
public class PostFetch extends AsyncTask<String, Void, String> {
    private String mTAG = "PostFetch";
    private AppUtils.MyInterface mListener;
    private Context mContext;

    public PostFetch(Context mContext, AppUtils.MyInterface mListener){
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

                if(args.length == 4){
                    data += "&" + URLEncoder.encode("uid", "UTF-8") + "="
                            + URLEncoder.encode(args[3], "UTF-8");
                }

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
