package com.example.socialapp.model;

import java.io.Serializable;

public class UserModel {

    private String id;
    private String name;
    private String email;
    private String avatar;
    private String liking;

    public UserModel() {
    }

    public UserModel(String avatar) {
        this.avatar = avatar;
    }

    public String getId() {
        return id;
    }

    public String getLiking() {
        return liking;
    }

    public void setLiking(String liking) {
        this.liking = liking;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
