package com.example.test.Models;

public class FindUsers {
    private String profileimage, FullName, UserName, Country;

    public FindUsers(){
    }

    public FindUsers(String profileimage, String FullName, String UserName, String Country){
        this.profileimage = profileimage;
        this.FullName = FullName;
        this.UserName = UserName;
    }

    public String getProfileImage(){
        return profileimage;
    }

    public void setProfileImage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFullName(){
        return FullName;
    }

    public void setFullName(String FullName){
        this.FullName = FullName;
    }

    public String getUserName(){
        return UserName;
    }

    public void setUserName(String UserName){
        this.UserName = UserName;
    }

    public String getCountry(){ return Country;}

    public void setCountry(String Country) {this.Country = Country;}
}

