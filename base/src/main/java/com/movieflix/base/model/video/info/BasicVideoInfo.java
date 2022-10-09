package com.movieflix.base.model.video.info;

public class BasicVideoInfo {
    String title;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    String description;

    @Override
    public String toString() {
        return "BasicVideoInfo {\n"+"description: "+description+"\ntitle: "+title+"\nposter: "+poster+"\n}";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    String poster;
}
