package com.movieflix.base.model.video.info;

public class Person {

    //maybe null
    //https://image.tmdb.org/t/p/ + width + profilePic
    String profilePic;

    //real name
    String title;

    //character or job
    String subtitle;

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
}
