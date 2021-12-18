package com.chatz.whatsapp.model;

import java.util.ArrayList;
import java.util.List;

public class NewsFeed {

    private String id;
    private String senderName;
    private String message;
    private String image;
    private String time;
    private String date;
    private List<String> listOfLikes = new ArrayList<>();
    private String senderAvatar;
    private String senderID;
    private int commentsCount = 0;

    public NewsFeed() {
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public List<String> getListOfLikes() {
        return listOfLikes;
    }

    public void setListOfLikes(List<String> listOfLikes) {
        this.listOfLikes = listOfLikes;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }
}
