package com.iitb.nihhaar.twitter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by Nihhaar on 10/8/2017.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<Posts> mDataset;
    private Context mContext;
    private int height;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mPostUser, mPostText, mComment;
        public CustomLinearLayout mCommentView;
        public ImageView mImageView;

        public ViewHolder(View view) {
            super(view);
            mPostUser = (TextView) view.findViewById(R.id.postuser);
            mPostText = (TextView) view.findViewById(R.id.posttext);
            mComment = (TextView) view.findViewById(R.id.comment);
            mCommentView = (CustomLinearLayout) view.findViewById(R.id.commentView);
            mImageView = (ImageView) view.findViewById(R.id.img_post);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(Context mContext, List<Posts> myDataset) {
        this.mContext = mContext;
        mDataset = myDataset;
        height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, mContext.getResources().getDisplayMetrics());
    }

    public void appendData(Posts post){
        mDataset.add(post);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_post, parent, false);

        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Posts posts = mDataset.get(position);
        holder.mPostUser.setText(posts.getPostUser());
        holder.mPostText.setText(posts.getPostText());

        final ImageView imageView = holder.mImageView;
        if(posts.isHasImage()) {
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            params.height = height;
            imageView.setLayoutParams(params);
            ImageFetchTask imageFetchTask = new ImageFetchTask(mContext, new MyInterface() {
                @Override
                public void myMethod(Bitmap result) {
                    imageView.setImageTintList(null);
                    imageView.setImageBitmap(result);
                }
            });
            String url = "http://" + AppUtils.servIP + ":" + AppUtils.servPort + "/" + AppUtils.webApp + "/GetPostImage";
            imageFetchTask.execute(url, String.valueOf(posts.getPostid()));
        }
        else {
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            params.height = 0;
            imageView.setLayoutParams(params);
            imageView.setImageResource(0);
            imageView.setImageBitmap(null);
        }

        final String postid = String.valueOf(posts.getPostid());
        holder.mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Handle add comment feature */
                Intent commentIntent = new Intent(mContext, CommentActivity.class);
                commentIntent.putExtra("postid", postid);
                mContext.startActivity(commentIntent);
            }
        });

        ArrayAdapter<Comments> mCommentsAdapter = new CommentsAdapter(mContext, posts.getComments());
        holder.mCommentView.setList(mContext, mCommentsAdapter);
        if(!posts.getComments().isEmpty()) {
            float multiplier = mContext.getResources().getDisplayMetrics().density;
            holder.mCommentView.setPadding(Math.round(10*multiplier), Math.round(10*multiplier), Math.round(10*multiplier), Math.round(10*multiplier));
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /* InputStream to ByteArray and then convert into Bitmap */
    public static Bitmap readFullyToBitmap(InputStream input, int height) throws IOException
    {
        byte[] buffer = new byte[8192];
        int bytesRead, length = 0;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, bytesRead);
            length += bytesRead;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(output.toByteArray(), 0, length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, height, height);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeByteArray(output.toByteArray(), 0, length, options);
        if(bitmap == null)
            Log.d("Bitmap", "Bitmap is actually null");

        return bitmap;
    }

    interface MyInterface {
        void myMethod(Bitmap result);
    }

    /* ImageFetchTask - AsyncTask for fetching post images */
    private class ImageFetchTask extends AsyncTask<String, Void, Bitmap> {
        private String mTAG = "ImageFetchTask";
        private MyInterface mListener;
        private Context mContext;

        public ImageFetchTask(Context mContext, MyInterface mListener){
            this.mContext = mContext;
            this.mListener = mListener;
        }

        @Override
        protected Bitmap doInBackground(String... args) {
            if(AppUtils.isNetworkAvailable(mContext)){
                try {
                    /* Authenticate with the server, if success store the credentials in shared prefs */
                    URLConnection conn = new URL(args[0]).openConnection();
                    conn.setDoOutput(true);

                    /* Create the post data */
                    String data = URLEncoder.encode("postid", "UTF-8")
                            + "=" + URLEncoder.encode(args[1], "UTF-8");

                    Log.d(mTAG, "Connecting to Server for Images for post " + args[1]);

                    /* Post the data */
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                    writer.write(data);
                    writer.flush();
                    writer.close();

                    /* Get the response from server and convert into bitmap */
                    return readFullyToBitmap(conn.getInputStream(), height);
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (mListener != null)
                mListener.myMethod(result);
        }
    }

}
