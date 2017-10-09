package com.iitb.nihhaar.twitter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Nihhaar on 10/9/2017.
 */

public class CommentsAdapter extends ArrayAdapter<Comments>{

    public CommentsAdapter(Context mContext, ArrayList<Comments> commentsArray){
        super(mContext, 0, commentsArray);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Comments comment = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_comment, parent, false);
        }

        // Populate the data into the template view using the data object
        ((TextView) convertView.findViewById(R.id.commentor)).setText(comment.getCommentor());
        ((TextView) convertView.findViewById(R.id.comment)).setText(comment.getComment());

        // Return the completed view to render on screen
        return convertView;
    }
}
