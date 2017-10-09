package com.iitb.nihhaar.twitter;

import java.util.ArrayList;

/**
 * Created by Nihhaar on 10/8/2017.
 */

public class Posts {
    private String postUser;
    private String postText;
    private ArrayList<Comments> comments;

    public void setPostUser(String postUser){
        this.postUser = postUser;
    }

    public void setPostText(String postText){
        this.postText = postText;
    }

    public void setComments(ArrayList<Comments> comments) {
        this.comments = comments;
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
}