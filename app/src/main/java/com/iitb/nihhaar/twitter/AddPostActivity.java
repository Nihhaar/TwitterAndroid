package com.iitb.nihhaar.twitter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class AddPostActivity extends AppCompatActivity {

    Button add_post;
    private EditText postText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        add_post = (Button)findViewById(R.id.post_button);
        postText = (EditText)findViewById(R.id.post_text);

        add_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPostTask addPostTask = new AddPostTask(AddPostActivity.this, new MyInterface() {
                    @Override
                    public void myMethod(String response) {
                        if(response.equals("0")){
                            Toast.makeText(AddPostActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                        }
                        else if(response.equals("1")){
                            Toast.makeText(AddPostActivity.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if(parseJsonData(response)) {
                                Intent main = new Intent(AddPostActivity.this, MainActivity.class);
                                startActivity(main);
                                Toast.makeText(AddPostActivity.this, "Successfully added post", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    }
                });
                String url = "http://" + AppUtils.servIP + ":" + AppUtils.servPort + "/" + AppUtils.webApp + "/CreatePost";
                addPostTask.execute(url, postText.getText().toString());
            }
        });
    }

    private Boolean parseJsonData(String jsonResponse){
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            Boolean status = jsonObject.getBoolean("status");
            if(status){
                return true;
            }
            else{
                Toast.makeText(AddPostActivity.this, "Unable to add post", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    interface MyInterface {
        void myMethod(String result);
    }

    /* AddPostTask - AsyncTask for adding post to server database */
    private class AddPostTask extends AsyncTask<String, Void, String> {
        private String mTAG = "AddPostTask";
        private MyInterface mListener;
        private Context mContext;

        public AddPostTask(Context mContext, MyInterface mListener){
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
                    String data = URLEncoder.encode("content", "UTF-8")
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
