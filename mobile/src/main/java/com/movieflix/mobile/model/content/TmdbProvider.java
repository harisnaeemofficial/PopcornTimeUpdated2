package com.movieflix.mobile.model.content;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import com.movieflix.base.model.video.info.BasicVideoInfo;
import com.movieflix.base.model.video.info.CinemaMoviesInfo;
import com.movieflix.base.model.video.info.CinemaTvShowsInfo;
import com.movieflix.base.model.video.info.MoviesInfo;
import com.movieflix.base.model.video.info.Person;
import com.movieflix.base.model.video.info.TvShowsInfo;
import com.movieflix.base.model.video.info.VideoInfo;
import com.movieflix.base.utils.Logger;
import com.movieflix.model.content.IDetailsProvider;
import com.movieflix.utils.GsonUtils;

public final class TmdbProvider implements IDetailsProvider {

    private final Api api;
    private final String key;

    //allowed people per department to be shown in the description
    private static Map<String, Integer> allowedPerDepartment = new HashMap<>();
    static {
        //not important for user
        allowedPerDepartment.put("Art", 2);
        allowedPerDepartment.put("Camera", 2);
        allowedPerDepartment.put("Costume & Make-Up", 1);
        allowedPerDepartment.put("Crew", 1);
        allowedPerDepartment.put("Lighting", 1);
        allowedPerDepartment.put("Visual Effects", 2);

        //more important for user
        allowedPerDepartment.put("Directing", 3);
        allowedPerDepartment.put("Editing", 3);
        allowedPerDepartment.put("Production", 4);
        allowedPerDepartment.put("Sound", 2);
        allowedPerDepartment.put("Writing", 3);
    }

    public TmdbProvider(@NonNull String url, @NonNull String key) {
        if (TextUtils.isEmpty(url)) {
            this.api = new Api() {
                @Override
                public Observable<Response<ResponseBody>> getTVShowCredits(String id, String apiKey) {
                    return Observable.just(Response.success(ResponseBody.create(MediaType.parse("application/json"),"")));
                }

                @Override
                public Observable<Response<ResponseBody>> getMovieCredits(String imdb, String apiKey) {
                    return Observable.just(Response.success(ResponseBody.create(MediaType.parse("application/json"),"")));
                }

                @Override
                public Observable<Response<ResponseBody>> getTVShowRecommendations(String id, String apiKey) {
                    return Observable.just(Response.success(ResponseBody.create(MediaType.parse("application/json"),"")));
                }

                @Override
                public Observable<Response<ResponseBody>> getMovieRecommendations(String id, String apiKey) {
                    return Observable.just(Response.success(ResponseBody.create(MediaType.parse("application/json"),"")));
                }

                @Override
                public Observable<Response<ResponseBody>> getMovieTmdb(String imdb, String apiKey) {
                    return Observable.just(Response.success(ResponseBody.create(MediaType.parse("application/json"),"")));
                }

                @Override
                public Observable<Response<ResponseBody>> getMovieInfo(@Path("imdb") String imdb, @Query("api_key") String apiKey) {
                    return Observable.just(Response.success(ResponseBody.create(MediaType.parse("application/json"),"")));
                }

                @Override
                public Observable<Response<ResponseBody>> getMovieBackdrops(@Path("imdb") String imdb, @Query("api_key") String apiKey) {
                    return Observable.just(Response.success(ResponseBody.create(MediaType.parse("application/json"),"")));
                }

                @Override
                public Observable<Response<ResponseBody>> search(@Query(value = "query", encoded = true) String title, @Query("api_key") String apiKey) {
                    return Observable.just(Response.success(ResponseBody.create(MediaType.parse("application/json"),"")));
                }

                @Override
                public Observable<Response<ResponseBody>> getTVShowInfo(@Path("id") String id, @Query("api_key") String apiKey) {
                    return Observable.just(Response.success(ResponseBody.create(MediaType.parse("application/json"),"")));
                }

                @Override
                public Observable<Response<ResponseBody>> getTVShowBackdrops(@Path("id") String id, @Query("api_key") String apiKey) {
                    return Observable.just(Response.success(ResponseBody.create(MediaType.parse("application/json"),"")));
                }
            };
        } else {
            this.api = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                    .baseUrl(url)
                    .build()
                    .create(Api.class);
        }
        this.key = key;
    }

    @Override
    public <T extends VideoInfo> boolean isDetailsExists(T videoInfo) {
        return videoInfo.getDurationMinutes() > 0 && videoInfo.getBackdrops() != null;
    }

    @NonNull
    @Override
    public Observable<? extends VideoInfo> getDetails(final VideoInfo videoInfo) {
        if (videoInfo instanceof CinemaMoviesInfo) {
            return api.getMovieTmdb(videoInfo.getImdb(), key).observeOn(AndroidSchedulers.mainThread()).concatMap(new MoviesTmdbIdRxMapper((MoviesInfo) videoInfo));
        } else if (videoInfo instanceof CinemaTvShowsInfo) {
            return api.search(videoInfo.getTitle(), key).observeOn(AndroidSchedulers.mainThread()).concatMap(new TVShowSearchRxMapper((CinemaTvShowsInfo) videoInfo));
        }
        return Observable.error(new IllegalArgumentException("Wrong video info type"));
    }

    private abstract class VideoInfoRxMapper<T extends VideoInfo> implements Function<Response<ResponseBody>, T> {

        final T videoInfo;

        VideoInfoRxMapper(T videoInfo) {
            this.videoInfo = videoInfo;
        }
    }

    private final class MoviesTmdbIdRxMapper implements Function<Response<ResponseBody>, ObservableSource<MoviesInfo>> {

        MoviesInfo videoInfo;
        MoviesTmdbIdRxMapper(MoviesInfo videoInfo) {
            this.videoInfo = videoInfo;
        }

        @Override
        public ObservableSource<MoviesInfo> apply(@io.reactivex.annotations.NonNull Response<ResponseBody> response) throws Exception {
            if (response.isSuccessful()) {
                final JsonObject jsonInfo = new JsonParser().parse(response.body().charStream()).getAsJsonObject();
                videoInfo.setTmdbID(GsonUtils.getAsInt(jsonInfo, "id"));
                String tmdb = String.valueOf(videoInfo.getTmdbID());
                return Observable.merge(
                        api.getMovieInfo(tmdb, key).map(new MoviesInfoRxMapper((CinemaMoviesInfo) videoInfo)),
                        api.getMovieBackdrops(tmdb, key).map(new BackdropsRxMapper<>((CinemaMoviesInfo) videoInfo)),
                        api.getMovieCredits(tmdb, key).map(new MovieCreditsRxMapper((CinemaMoviesInfo) videoInfo)),
                        api.getMovieRecommendations(tmdb, key).map(new MovieRecommendationsRxMapper((CinemaMoviesInfo) videoInfo)))
                        .observeOn(AndroidSchedulers.mainThread());
            } else {
                Logger.debug("response MoviesInfoRxMapper not successful: "+response.errorBody().string());
            }
            return Observable.just(videoInfo);
        }
    }

    private final class MoviesInfoRxMapper extends VideoInfoRxMapper<MoviesInfo> {

        MoviesInfoRxMapper(MoviesInfo videoInfo) {
            super(videoInfo);
        }

        @Override
        public MoviesInfo apply(@io.reactivex.annotations.NonNull Response<ResponseBody> response) throws Exception {
            if (response.isSuccessful()) {
                final JsonObject jsonInfo = new JsonParser().parse(response.body().charStream()).getAsJsonObject();
                videoInfo.setDurationMinutes(GsonUtils.getAsInt(jsonInfo, "runtime"));
                videoInfo.setTmdbID(GsonUtils.getAsInt(jsonInfo, "id"));
                videoInfo.setBudgetInUSD(GsonUtils.getAsLong(jsonInfo, "budget"));
                videoInfo.setRevenueInUSD(GsonUtils.getAsLong(jsonInfo, "revenue"));
                videoInfo.setHomepage(GsonUtils.getAsString(jsonInfo, "homepage"));

                ArrayList<String> productionCountries = new ArrayList<>();
                for (JsonElement productionCountry : jsonInfo.getAsJsonArray("production_countries")) {
                    productionCountries.add(GsonUtils.getAsString((JsonObject) productionCountry, "name"));
                }
                videoInfo.setProductionCountries(productionCountries);

                ArrayList<String> productionCompanies = new ArrayList<>();
                for (JsonElement productionCompany : jsonInfo.getAsJsonArray("production_companies")) {
                    productionCompanies.add(GsonUtils.getAsString((JsonObject) productionCompany, "name"));
                }
                videoInfo.setProductionCompanies(productionCompanies);
            } else {
                Logger.debug("response MoviesInfoRxMapper not successful: "+response.errorBody().string());
            }
            return videoInfo;
        }
    }

    private final class TvShowsInfoRxMapper extends VideoInfoRxMapper<TvShowsInfo> {

        TvShowsInfoRxMapper(TvShowsInfo videoInfo) {
            super(videoInfo);
        }

        @Override
        public TvShowsInfo apply(@io.reactivex.annotations.NonNull Response<ResponseBody> response) throws Exception {
            if (response.isSuccessful()) {
                final JsonObject jsonInfo = new JsonParser().parse(response.body().charStream()).getAsJsonObject();
                final JsonArray jsonEpisodeRunTime = jsonInfo.getAsJsonArray("episode_run_time");
                if (jsonEpisodeRunTime.size() > 0) {
                    videoInfo.setDurationMinutes(jsonEpisodeRunTime.get(0).getAsInt());
                }

                videoInfo.setTmdbID(GsonUtils.getAsInt(jsonInfo, "id"));

                videoInfo.setHomepage(GsonUtils.getAsString(jsonInfo, "homepage"));

                ArrayList<String> productionCountries = new ArrayList<>();
                for (JsonElement productionCountry : jsonInfo.getAsJsonArray("origin_country")) {
                    Locale loc = new Locale("",productionCountry.getAsString());
                    productionCountries.add(loc.getDisplayCountry());
                }
                videoInfo.setProductionCountries(productionCountries);

                Map<String, List<Person>> creators = new HashMap<>();

                ArrayList<Person> creatorsList = new ArrayList<>();
                for (JsonElement creatorJSON : jsonInfo.getAsJsonArray("created_by")) {
                    Person creator = new Person();
                    creator.setProfilePic(GsonUtils.getAsString((JsonObject) creatorJSON, "profile_path"));
                    creator.setSubtitle("Creator");
                    creator.setTitle(GsonUtils.getAsString((JsonObject) creatorJSON, "name"));
                    creatorsList.add(creator);
                }
                creators.put("Creators", creatorsList);
                videoInfo.setCrewMembersByDepartment(creators);

                ArrayList<String> productionCompanies = new ArrayList<>();
                for (JsonElement productionCompany : jsonInfo.getAsJsonArray("production_companies")) {
                    productionCompanies.add(GsonUtils.getAsString((JsonObject) productionCompany, "name"));
                }
                videoInfo.setProductionCompanies(productionCompanies);

            } else {
                Logger.debug("response TvShowsInfoRxMapper not successful: "+response.errorBody().string());
            }
            return videoInfo;
        }
    }

    private final class MovieCreditsRxMapper extends VideoInfoRxMapper<MoviesInfo> {

        MovieCreditsRxMapper(MoviesInfo videoInfo) {
            super(videoInfo);
        }

        @Override
        public MoviesInfo apply(@io.reactivex.annotations.NonNull Response<ResponseBody> response) throws Exception {
            if (response.isSuccessful()) {
                final JsonObject jsonInfo = new JsonParser().parse(response.body().charStream()).getAsJsonObject();

                Map<String, List<Person>> crew = new HashMap<>();

                List<Person> artList = new ArrayList<>();
                List<Person> cameraList = new ArrayList<>();
                List<Person> costumeNmakeupList = new ArrayList<>();
                List<Person> crewList = new ArrayList<>();
                List<Person> lightingList = new ArrayList<>();
                List<Person> visualsList = new ArrayList<>();
                List<Person> directingList = new ArrayList<>();
                List<Person> editingList = new ArrayList<>();
                List<Person> productionList = new ArrayList<>();
                List<Person> soundList = new ArrayList<>();
                List<Person> writingList = new ArrayList<>();

                for (JsonElement crewMemberJSON : jsonInfo.getAsJsonArray("crew")) {
                    String department = GsonUtils.getAsString((JsonObject) crewMemberJSON, "department");
                    int allowedInDepartment = allowedPerDepartment.get(department);
                    if (allowedInDepartment > 0) {
                        allowedPerDepartment.put(department, allowedInDepartment - 1);
                        Person crewMember = new Person();
                        crewMember.setProfilePic(GsonUtils.getAsString((JsonObject) crewMemberJSON, "profile_path"));
                        crewMember.setTitle(GsonUtils.getAsString((JsonObject) crewMemberJSON, "name"));
                        crewMember.setSubtitle(GsonUtils.getAsString((JsonObject) crewMemberJSON, "job"));

                        if (department != null) {
                            switch (department) {
                                case "Art":
                                    artList.add(crewMember);
                                    break;
                                case "Camera":
                                    cameraList.add(crewMember);
                                    break;
                                case "Costume & Make-Up":
                                    costumeNmakeupList.add(crewMember);
                                    break;
                                case "Crew":
                                    crewList.add(crewMember);
                                    break;
                                case "Lighting":
                                    lightingList.add(crewMember);
                                    break;
                                case "Visual Effects":
                                    visualsList.add(crewMember);
                                    break;
                                case "Directing":
                                    directingList.add(crewMember);
                                    break;
                                case "Editing":
                                    editingList.add(crewMember);
                                    break;
                                case "Production":
                                    productionList.add(crewMember);
                                    break;
                                case "Sound":
                                    soundList.add(crewMember);
                                    break;
                                case "Writing":
                                    writingList.add(crewMember);
                                    break;
                            }
                        }
                    } else {
                        break;
                    }
                }

                crew.put("Art", artList);
                crew.put("Camera", cameraList);
                crew.put("Costume & Make-Up",costumeNmakeupList);
                crew.put("Crew", crewList);
                crew.put("Lighting", lightingList);
                crew.put("Visual Effects",visualsList);
                crew.put("Directing", directingList);
                crew.put("Editing", editingList);
                crew.put("Production", productionList);
                crew.put("Sound", soundList);
                crew.put("Writing", writingList);

                videoInfo.setCrewMembersByDepartment(crew);

                ArrayList<Person> actors = new ArrayList<>();

                JsonArray jsonCrew = jsonInfo.getAsJsonArray("cast");
                for (int i = 0; i < 13; i++) {
                    JsonElement actorJSON = jsonCrew.get(i);
                    Person actor = new Person();
                    actor.setProfilePic(GsonUtils.getAsString((JsonObject) actorJSON, "profile_path"));
                    actor.setTitle(GsonUtils.getAsString((JsonObject) actorJSON, "name"));
                    actor.setSubtitle(GsonUtils.getAsString((JsonObject) actorJSON, "character"));
                    Logger.debug("actor of "+i+": "+actor.getTitle());
                    actors.add(actor);
                }
                videoInfo.setCast(actors);
            } else {
                Logger.debug("response MovieCreditsRxMapper not successful(tmdb id: "+videoInfo.getTmdbID()+"): "+response.errorBody().string());
            }
            return videoInfo;
        }
    }

    public class ContentActor {

        @SerializedName("profile_path")
        private String profilePic;

        @SerializedName("name")
        private String name;

        @SerializedName("character")
        private String character;

        public String getProfilePic() {
            return profilePic;
        }

        public void setProfilePic(String profilePic) {
            this.profilePic = profilePic;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCharacter() {
            return character;
        }

        public void setCharacter(String character) {
            this.character = character;
        }
    }

    private final class TvShowsCreditsRxMapper extends VideoInfoRxMapper<TvShowsInfo> {

        TvShowsCreditsRxMapper(TvShowsInfo videoInfo) {
            super(videoInfo);
        }

        @Override
        public TvShowsInfo apply(@io.reactivex.annotations.NonNull Response<ResponseBody> response) throws Exception {
            if (response.isSuccessful()) {
                final JsonObject jsonInfo = new JsonParser().parse(response.body().charStream()).getAsJsonObject();

                int maxActors = 12;
                ArrayList<Person> actors = new ArrayList<>();
                for (JsonElement actorJSON : jsonInfo.getAsJsonArray("cast")) {
                    if (maxActors > 0) {
                        maxActors -= 1;
                        Person actor = new Person();
                        actor.setProfilePic(GsonUtils.getAsString((JsonObject) actorJSON, "profile_path"));
                        actor.setTitle(GsonUtils.getAsString((JsonObject) actorJSON, "name"));
                        actor.setSubtitle(GsonUtils.getAsString((JsonObject) actorJSON, "character"));
                        Logger.debug("actor of "+videoInfo.getTitle()+": "+actor.getTitle());
                        actors.add(actor);
                    } else {
                        break;
                    }
                }
                videoInfo.setCast(actors);
            } else {
                Logger.debug("response TvShowsCreditsRxMapper not successful: "+response.errorBody().string());
            }
            return videoInfo;
        }
    }

    private final class MovieRecommendationsRxMapper extends VideoInfoRxMapper<MoviesInfo> {

        MovieRecommendationsRxMapper(MoviesInfo videoInfo) {
            super(videoInfo);
        }

        @Override
        public MoviesInfo apply(@io.reactivex.annotations.NonNull Response<ResponseBody> response) throws Exception {
            if (response.isSuccessful()) {
                final JsonObject jsonInfo = new JsonParser().parse(response.body().charStream()).getAsJsonObject();

                List<BasicVideoInfo> recommended = new ArrayList<>();
                int maxRecommendations = 12;
                for (JsonElement recommendationJSON : jsonInfo.getAsJsonArray("results")) {
                    maxRecommendations -= 1;
                    if (maxRecommendations < 0) {
                        break;
                    }
                    BasicVideoInfo recommendation = new BasicVideoInfo();
                    recommendation.setTitle(GsonUtils.getAsString((JsonObject) recommendationJSON, "title"));
                    recommendation.setPoster(GsonUtils.getAsString((JsonObject) recommendationJSON, "poster_path"));
                    recommendation.setDescription(GsonUtils.getAsString((JsonObject) recommendationJSON, "overview"));
                    //Logger.debug("recommendation for "+videoInfo.getTitle()+": "+recommendation.toString());
                    recommended.add(recommendation);
                }
                videoInfo.setRecommendedMOVIESorTVShows(recommended);
            } else {
                Logger.debug("response MovieRecommendationsRxMapper not successful(tmdb id: "+videoInfo.getTmdbID()+"): "+response.errorBody().string());
            }
            return videoInfo;
        }
    }

    private final class TvShowsRecommendationsRxMapper extends VideoInfoRxMapper<TvShowsInfo> {

        TvShowsRecommendationsRxMapper(TvShowsInfo videoInfo) {
            super(videoInfo);
        }

        @Override
        public TvShowsInfo apply(@io.reactivex.annotations.NonNull Response<ResponseBody> response) throws Exception {
            if (response.isSuccessful()) {
                final JsonObject jsonInfo = new JsonParser().parse(response.body().charStream()).getAsJsonObject();

                List<BasicVideoInfo> recommended = new ArrayList<>();
                int maxRecommendations = 12;
                for (JsonElement recommendationJSON : jsonInfo.getAsJsonArray("results")) {
                    maxRecommendations -= 1;
                    if (maxRecommendations < 0) {
                        break;
                    }
                    BasicVideoInfo recommendation = new BasicVideoInfo();
                    recommendation.setTitle(GsonUtils.getAsString((JsonObject) recommendationJSON, "name"));
                    recommendation.setPoster(GsonUtils.getAsString((JsonObject) recommendationJSON, "poster_path"));
                    recommendation.setDescription(GsonUtils.getAsString((JsonObject) recommendationJSON, "overview"));
                    //Logger.debug("recommendation for "+videoInfo.getTitle()+": "+recommendation.toString());
                    recommended.add(recommendation);
                }
                videoInfo.setRecommendedMOVIESorTVShows(recommended);
            } else {
                Logger.debug("response TvShowsRecommendationsRxMapper not successful: "+response.errorBody().string());
            }
            return videoInfo;
        }
    }

    private final class TVShowSearchRxMapper implements Function<Response<ResponseBody>, ObservableSource<TvShowsInfo>> {

        private final TvShowsInfo videoInfo;

        TVShowSearchRxMapper(TvShowsInfo videoInfo) {
            this.videoInfo = videoInfo;
        }

        @Override
        public ObservableSource<TvShowsInfo> apply(@io.reactivex.annotations.NonNull Response<ResponseBody> responseBodyResponse) throws Exception {
            if (responseBodyResponse.isSuccessful()) {
                final JsonArray jsonResults = new JsonParser().parse(responseBodyResponse.body().charStream()).getAsJsonObject().getAsJsonArray("results");
                if (jsonResults.size() == 1) {
                    return getTvShowsInfo(jsonResults.get(0).getAsJsonObject());
                } else if (jsonResults.size() > 1) {
                    final int year = videoInfo.getYear();
                    if (year > 0) {
                        for (JsonElement jsonElement : jsonResults) {
                            final JsonObject jsonResult = jsonElement.getAsJsonObject();
                            final String date = GsonUtils.getAsString(jsonResult, "first_air_date");
                            if (!TextUtils.isEmpty(date)) {
                                final String[] splitDate = date.split("-");
                                if (splitDate.length == 3 && year == Integer.parseInt(splitDate[0])) {
                                    return getTvShowsInfo(jsonResult);
                                }
                            }
                        }
                    }
                    return getTvShowsInfo(jsonResults.get(0).getAsJsonObject());
                }
                return Observable.just(videoInfo);
            } else {
                Logger.debug("response TVShowSearchRxMapper not successful: "+responseBodyResponse.errorBody().string());
            }
            throw new Exception(responseBodyResponse.errorBody().string());
        }

        private ObservableSource<TvShowsInfo> getTvShowsInfo(@NonNull JsonObject jsonObject) {
            final String id = GsonUtils.getAsString(jsonObject, "id");
            if (TextUtils.isEmpty(id)) {
                return Observable.just(videoInfo);
            }
            return Observable.merge(
                    api.getTVShowInfo(id, key).map(new TvShowsInfoRxMapper(videoInfo)),
                    api.getTVShowCredits(id, key).map(new TvShowsCreditsRxMapper(videoInfo)),
                    api.getTVShowBackdrops(id, key).map(new BackdropsRxMapper<>(videoInfo)),
                    api.getTVShowRecommendations(id, key).map(new TvShowsRecommendationsRxMapper(videoInfo))
            ).observeOn(AndroidSchedulers.mainThread());
        }
    }

    private final class BackdropsRxMapper<T extends VideoInfo> implements Function<Response<ResponseBody>, T> {

        private static final String FORMAT_BACKDROP_URL = "http://image.tmdb.org/t/p/w780%s";

        private final T videoInfo;

        private BackdropsRxMapper(T videoInfo) {
            this.videoInfo = videoInfo;
        }

        @Override
        public T apply(@io.reactivex.annotations.NonNull Response<ResponseBody> responseBodyResponse) throws Exception {
            if (responseBodyResponse.isSuccessful()) {
                final JsonArray jsonBackdrops = new JsonParser().parse(responseBodyResponse.body().charStream()).getAsJsonObject().getAsJsonArray("backdrops");
                final String[] backdrops = new String[jsonBackdrops.size()];
                for (int i = 0; i < jsonBackdrops.size(); i++) {
                    backdrops[i] = String.format(Locale.ENGLISH, FORMAT_BACKDROP_URL, jsonBackdrops.get(i).getAsJsonObject().get("file_path").getAsString());
                }
                videoInfo.setBackdrops(backdrops);
                return videoInfo;
            } else {
                Logger.debug("response BackdropsRxMapper not successful: "+responseBodyResponse.errorBody().string());
            }
            throw new Exception(responseBodyResponse.errorBody().string());
        }
    }

    private interface Api {

        @GET("3/movie/{imdb}")
        Observable<Response<ResponseBody>> getMovieInfo(@Path("imdb") String imdb, @Query("api_key") String apiKey);

        @GET("3/movie/{imdb}/images")
        Observable<Response<ResponseBody>> getMovieBackdrops(@Path("imdb") String imdb, @Query("api_key") String apiKey);

        @GET("3/search/tv")
        Observable<Response<ResponseBody>> search(@Query(value = "query", encoded = true) String title, @Query("api_key") String apiKey);

        @GET("3/tv/{id}")
        Observable<Response<ResponseBody>> getTVShowInfo(@Path("id") String id, @Query("api_key") String apiKey);

        @GET("3/tv/{id}/images")
        Observable<Response<ResponseBody>> getTVShowBackdrops(@Path("id") String id, @Query("api_key") String apiKey);

        //IMDB details api
        @GET("3/tv/{id}/credits")
        Observable<Response<ResponseBody>> getTVShowCredits(@Path("id") String id, @Query("api_key") String apiKey);

        @GET("3/movie/{imdb}/credits")
        Observable<Response<ResponseBody>> getMovieCredits(@Path("imdb") String imdb, @Query("api_key") String apiKey);

        @GET("3/tv/{id}/recommendations")
        Observable<Response<ResponseBody>> getTVShowRecommendations(@Path("id") String id, @Query("api_key") String apiKey);

        @GET("3/movie/{id}/recommendations")
        Observable<Response<ResponseBody>> getMovieRecommendations(@Path("id") String id, @Query("api_key") String apiKey);

        @GET("3/movie/{imdb}/external_ids")
        Observable<Response<ResponseBody>> getMovieTmdb(@Path("imdb") String imdb, @Query("api_key") String apiKey);
    }
}
