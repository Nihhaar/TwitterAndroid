package com.iitb.nihhaar.twitter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AddPostActivity extends AppCompatActivity {

    private Button add_post;
    private EditText postText;
    private ImageView imageView;
    private Uri imageURI;
    private static final int SELECT_PICTURE = 100;
    private static String TAG = "AddPostActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        add_post = (Button)findViewById(R.id.post_button);
        postText = (EditText)findViewById(R.id.post_text);
        imageView = (ImageView)findViewById(R.id.post_image);
        imageURI = null;

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100);
            }
        });

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                // Get the url from data
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    // Set the image in ImageView and imageURI
                    imageURI = selectedImageUri;
                    imageView.setImageTintList(null);
                    imageView.setImageURI(selectedImageUri);
                }
            }
        }
    }

    interface MyInterface {
        void myMethod(String result);
    }

    /* AddPostTask - AsyncTask for adding post to server database */
    private class AddPostTask extends AsyncTask<String, Void, String> {
        private String mTAG = "AddPostTask";
        private ProgressDialog mDialog;
        private MyInterface mListener;
        private Context mContext;

        public AddPostTask(Context mContext, MyInterface mListener){
            mDialog = new ProgressDialog(mContext);
            this.mContext = mContext;
            this.mListener = mListener;
        }

        @Override
        protected void onPreExecute() {
            mDialog.setMessage("Please wait...");
            mDialog.setIndeterminate(true);
            mDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

            String result = "0";
            if(AppUtils.isNetworkAvailable(mContext)){
                try {
                    MultiPartUtility multipart = new MultiPartUtility(args[0], "UTF-8");
                    multipart.addFormField("content", args[1]);

                    if(imageURI != null) {
                        Bitmap bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageURI));
                        multipart.addFilePart("image", "post.jpg", bm);
                    }

                    result = multipart.finish();
                    Log.d(mTAG, result);
                } catch (IOException ex){
                    result = "1";
                    ex.printStackTrace();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            mDialog.dismiss();
            if (mListener != null)
                mListener.myMethod(result);
        }
    }
}
