package pt.pinho.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by luispinho on 22/02/2018.
 */

class Movie implements Parcelable {

    private String vote_count, id, vote_average, title, popularity, poster, original_lang, original_title, background,
    adult, overview, release_date;
    private ArrayList<String> genres;


    Movie(String vote_count, String id, String vote_average, String title, String popularity, String poster, String original_lang, String original_title, String background, String adult,
                 String overview, String release_date, ArrayList<String> genres) {
        this.vote_count = vote_count;
        this.id = id;
        this.vote_average = vote_average;
        this.title = title;
        this.popularity = popularity;
        this.poster = poster;
        this.original_lang = original_lang;
        this.original_title = original_title;
        this.background = background;
        this.adult = adult;
        this.overview = overview;
        this.release_date = release_date;
        this.genres = genres;
    }

    private Movie(Parcel in) {
        vote_count = in.readString();
        id = in.readString();
        vote_average = in.readString();
        title = in.readString();
        popularity = in.readString();
        poster = in.readString();
        original_lang = in.readString();
        original_title = in.readString();
        background = in.readString();
        adult = in.readString();
        overview = in.readString();
        release_date = in.readString();
        genres = in.createStringArrayList();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getVote_count() {
        return vote_count;
    }

    public void setVote_count(String vote_count) {
        this.vote_count = vote_count;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVote_average() {
        return vote_average;
    }

    public void setVote_average(String vote_average) {
        this.vote_average = vote_average;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPopularity() {
        return popularity;
    }

    public void setPopularity(String popularity) {
        this.popularity = popularity;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getOriginal_lang() {
        return original_lang;
    }

    public void setOriginal_lang(String original_lang) {
        this.original_lang = original_lang;
    }

    public String getOriginal_title() {
        return original_title;
    }

    public void setOriginal_title(String original_title) {
        this.original_title = original_title;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getAdult() {
        return adult;
    }

    public void setAdult(String adult) {
        this.adult = adult;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }

    public void setGenres(ArrayList<String> genres) {
        this.genres = genres;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(vote_count);
        dest.writeString(id);
        dest.writeString(vote_average);
        dest.writeString(title);
        dest.writeString(popularity);
        dest.writeString(poster);
        dest.writeString(original_lang);
        dest.writeString(original_title);
        dest.writeString(background);
        dest.writeString(adult);
        dest.writeString(overview);
        dest.writeString(release_date);
        dest.writeStringList(genres);
    }
}
