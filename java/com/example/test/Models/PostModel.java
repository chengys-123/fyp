package com.example.test.Models;

public class PostModel {
    String posttitle, postdescription, postimage, posttime, postname, image, pid, uid, pLikes, currentPlaceName, placeId;

    public PostModel(){}

    public PostModel(String posttitle, String postdescription, String postimage, String posttime,
                     String postname, String image, String uid, String pid, String likes, String currentplace, String placeid) {
        this.posttitle = posttitle;
        this.postdescription = postdescription;
        this.postimage = postimage;
        this.posttime = posttime;
        this.postname = postname;
        this.image = image;
        this.uid = uid;
        this.pid = pid;
        this.pLikes = likes;
        this.currentPlaceName = currentplace;
        this.placeId = placeid;
    }

    public void setPlaceId(String placeid) {this.placeId = placeid;}

    public void setCurrentplace(String currentplace) {this.currentPlaceName = currentplace;}

    public void setpLikes(String likes) {this.pLikes = pLikes;}

    public void setPid(String pid) {this.pid = pid;}

    public void setUid(String uid) {this.uid = uid;}

    public void setPosttitle(String posttitle) {
        this.posttitle = posttitle;
    }

    public void setPostdescription(String postdescription) { this.postdescription = postdescription; }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public void setPosttime(String posttime) {
        this.posttime = posttime;
    }

    public void setPostname(String name) {
        this.postname = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getpLikes() {return pLikes; }

    public String getPosttitle() {
        return posttitle;
    }

    public String getPostdescription() {
        return postdescription;
    }

    public String getPostimage() {
        return postimage;
    }

    public String getPosttime() {
        return posttime;
    }

    public String getPostname() {
        return postname;
    }

    public String getImage() {
        return image;
    }

    public String getPid() {return pid;}

    public String getUid() {return uid;}

    public String getCurrentplace(){return currentPlaceName;}

    public String getPlaceId(){return placeId;}
}
