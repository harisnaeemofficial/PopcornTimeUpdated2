package com.movieflix.base.model.video.info;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VideoInfo {

    private final String type;

    private int tmdbID = 0;

    private String imdb;
    private String title;
    private int year;
    private float rating;
    private int durationMinutes;
    private String description;
    private String actors;

    private String poster;
    private String posterBig;
    private String trailer;

    private String[] genres;
    private String[] backdrops;

    //TODO: imlement imdb info in the app, backend is ready
    //IMDB info
    private long budgetInUSD;
    private long revenueInUSD;
    private String homepage;
    private ArrayList<String> productionCompanies;
    private ArrayList<String> productionCountries;
    private ArrayList<Person> cast;

    //      Department, members
    private Map<String, List<Person>> crewMembersByDepartment;
    private List<BasicVideoInfo> recommendedMOVIESorTVShows;

    public long getBudgetInUSD() {
        return budgetInUSD;
    }

    public void setBudgetInUSD(long budgetInUSD) {
        this.budgetInUSD = budgetInUSD;
    }

    public int getTmdbID() {
        return tmdbID;
    }

    public void setTmdbID(int tmdbID) {
        this.tmdbID = tmdbID;
    }

    public long getRevenueInUSD() {
        return revenueInUSD;
    }

    public void setRevenueInUSD(long revenueInUSD) {
        this.revenueInUSD = revenueInUSD;
    }

    public Map<String, List<Person>> getCrewMembersByDepartment() {
        return crewMembersByDepartment;
    }

    public void setCrewMembersByDepartment(Map<String, List<Person>> crewMembersByDepartment) {
        this.crewMembersByDepartment = crewMembersByDepartment;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public List<String> getProductionCompanies() {
        return productionCompanies;
    }

    public void setProductionCompanies(ArrayList<String> productionCompanies) {
        this.productionCompanies = productionCompanies;
    }

    public List<String> getProductionCountries() {
        return productionCountries;
    }

    public void setProductionCountries(ArrayList<String> productionCountries) {
        this.productionCountries = productionCountries;
    }

    public List<Person> getCast() {
        return cast;
    }

    public void setCast(ArrayList<Person> cast) {
        this.cast = cast;
    }

    public List<BasicVideoInfo> getRecommendedMOVIESorTVShows() {
        return recommendedMOVIESorTVShows;
    }

    public void setRecommendedMOVIESorTVShows(List<BasicVideoInfo> recommendedMOVIESorTVShows) {
        this.recommendedMOVIESorTVShows = recommendedMOVIESorTVShows;
    }



    public VideoInfo(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getImdb() {
        return imdb;
    }

    public void setImdb(String imdb) {
        this.imdb = imdb;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getPosterBig() {
        return posterBig;
    }

    public void setPosterBig(String posterBig) {
        this.posterBig = posterBig;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public String[] getGenres() {
        return genres;
    }

    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    public String[] getBackdrops() {
        return backdrops;
    }

    public void setBackdrops(String[] backdrops) {
        this.backdrops = backdrops;
    }

}