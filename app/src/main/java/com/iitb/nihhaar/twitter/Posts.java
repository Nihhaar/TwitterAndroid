package com.iitb.nihhaar.twitter;

import java.util.ArrayList;

/**
 * Created by Nihhaar on 10/8/2017.
 */

public class Posts {
    private String postUser;
    private String postText;
    private ArrayList<Comments> comments;
    private int postid;
    private boolean hasImage;

    public void setPostUser(String postUser){
        this.postUser = postUser;
    }

    public void setPostText(String postText){
        this.postText = postText;
    }

    public void setComments(ArrayList<Comments> comments) {
        this.comments = comments;
    }

    public void setPostid(int postid) {
        this.postid = postid;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    public String getPostText() {
        return postText;
    }

    public String getPostUser() {
        return postUser;
    }

    public ArrayList<Comments> getComments() {
        return comments;
    }

    public int getPostid() {
        return postid;
    }

    public boolean isHasImage() {
        return hasImage;
    }
}
