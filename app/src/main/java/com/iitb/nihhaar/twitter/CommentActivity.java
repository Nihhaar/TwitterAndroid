package com.iitb.nihhaar.twitter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class CommentActivity extends AppCompatActivity {

    private Button addComment;
    private TextView commentText;
    private String postid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        addComment = (Button) findViewById(R.id.comment_button);
        commentText = (TextView) findViewById(R.id.comment_text);
        postid = getIntent().getStringExtra("postid");

        addComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentAsyncTask commentAsyncTask = new CommentAsyncTask(CommentActivity.this, new AppUtils.MyInterface() {
                    @Override
                    public void myMethod(String result) {
                        if(result.equals("0")){
                            Toast.makeText(CommentActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                        }
                        else if(result.equals("1")){
                            Toast.makeText(CommentActivity.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                Boolean status = jsonObject.getBoolean("status");
                                if (status) {
                                    Toast.makeText(CommentActivity.this, "Successfully posted comment", Toast.LENGTH_SHORT).show();
                                }
                                else
                                    Toast.makeText(CommentActivity.this, "Unable to post comment", Toast.LENGTH_SHORT).show();
                            } catch (JSONException ex){
                                ex.printStackTrace();
                            }
                        }
                    }
                });
                String url = "http://" + AppUtils.servIP + ":" + AppUtils.servPort + "/" + AppUtils.webApp + "/NewComment";
                commentAsyncTask.execute(url, postid, commentText.getText().toString());

                Intent main = new Intent(CommentActivity.this, MainActivity.class);
                startActivity(main);
                finish();
            }
        });
    }
}
