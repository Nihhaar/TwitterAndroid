package com.iitb.nihhaar.twitter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nihhaar on 10/8/2017.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<Posts> mDataset;
    private Context mContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mPostUser, mPostText, mComment;
        public CustomLinearLayout mCommentView;

        public ViewHolder(View view) {
            super(view);
            mPostUser = (TextView) view.findViewById(R.id.postuser);
            mPostText = (TextView) view.findViewById(R.id.posttext);
            mComment = (TextView) view.findViewById(R.id.comment);
            mCommentView = (CustomLinearLayout) view.findViewById(R.id.commentView);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(Context mContext, List<Posts> myDataset) {
        this.mContext = mContext;
        mDataset = myDataset;
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
        holder.mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Handle add comment feature */
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
}
