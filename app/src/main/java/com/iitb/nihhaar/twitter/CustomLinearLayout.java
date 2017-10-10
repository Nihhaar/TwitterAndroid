package com.iitb.nihhaar.twitter;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Nihhaar on 10/9/2017.
 */

public class CustomLinearLayout extends LinearLayout{

    private Adapter list;

    public CustomLinearLayout(Context context){
        super(context);
    }

    public CustomLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public CustomLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setList(final Context context, final Adapter list) {
        this.list = list;
        this.removeAllViews();

        //Populate list
        if (this.list!=null){
            for (int i=0;i<this.list.getCount();i++){
                View item= list.getView(i, null, null);
                if(i>2)
                    item.setVisibility(View.GONE);
                this.addView(item);

                if(i==2){
                    TextView moreComments = new TextView(context);
                    moreComments.setText("More Comments");
                    moreComments.setTextColor(context.getResources().getColor(R.color.colorAccent, null));
                    moreComments.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View v){
                            setVisibleList(context, list);
                        }
                    });
                    this.addView(moreComments);
                }
            }
        }
    }

    public void setVisibleList(final Context context, final Adapter list) {
        this.list = list;
        this.removeAllViews();
        //Populate list
        if (this.list!=null){
            for (int i=0;i<this.list.getCount();i++){
                View item= list.getView(i, null, null);
                this.addView(item);
                if(i==this.list.getCount()-1){
                    TextView lessComments = new TextView(context);
                    lessComments.setText("Less Comments");
                    lessComments.setTextColor(context.getResources().getColor(R.color.colorAccent, null));
                    lessComments.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View v){
                            setList(context, list);
                        }
                    });
                    this.addView(lessComments);
                }
            }
        }
    }
}
