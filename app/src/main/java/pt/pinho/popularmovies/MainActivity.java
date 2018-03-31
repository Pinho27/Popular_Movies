package pt.pinho.popularmovies;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Context ctx;
    private final String apiKeyTmdb = BuildConfig.API_KEY;
    private String list_order  = "popular";

    HashMap<String, String> hashMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null)
            list_order = savedInstanceState.getString("list_order");

        ctx = this;
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);

        int numberColumns = 2;

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);


        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_popular:
                                list_order = "popular";
                                getGenres();
                                break;

                            case R.id.action_rating:
                                list_order = "top_rated";
                                getGenres();
                                break;

                            case R.id.action_favourites: getFavouriteMovies();
                            list_order = "favourites";
                            break;

                        }
                        return true;
                    }
                });

         int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            numberColumns = 3;

        recyclerView.setLayoutManager(new GridLayoutManager(this, numberColumns));

        hashMap = getGenres();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("list_order", list_order);
    }


    void getMovies(String type){
        final String url = "http://api.themoviedb.org/3/movie/" + type + "?api_key=" + apiKeyTmdb + "&append_to_response=videos";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        JSONArray a = null;
                        try {
                            a = response.getJSONArray("results");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        createAdapter(parseJson(a));

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ctx, getResources().getString(R.string.network_error_movies), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsObjRequest);
    }

    void getFavouriteMovies(){
        Cursor favourites = getContentResolver().query(
                ContentProvider.CONTENT_URI, null, null, null, null);

        List<Movie> movieList = new ArrayList<>();

        assert favourites != null;
        while(favourites.moveToNext()) {

            movieList.add(new Movie(null, favourites.getString(0), favourites.getString(1), favourites.getString(2), null, favourites.getString(3),
                    null, null, favourites.getString(6), null, favourites.getString(4), favourites.getString(5), null));
        }
        favourites.close();

        createAdapter(movieList);
    }

    void createAdapter(List<Movie> movieList){
        MoviesAdapter moviesAdapter = new MoviesAdapter(ctx, movieList);
        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(moviesAdapter);
        alphaAdapter.setDuration(2000);
        alphaAdapter.setInterpolator(new AnticipateInterpolator());
        alphaAdapter.setFirstOnly(true);
        recyclerView.setAdapter(new SlideInBottomAnimationAdapter(alphaAdapter));
        recyclerView.setVisibility(View.VISIBLE);
    }

    HashMap<String, String> getGenres(){

        if(list_order.equals("favourites")){
            getFavouriteMovies();
            return null;
        }

        final String url = "https://api.themoviedb.org/3/genre/movie/list?api_key=" + apiKeyTmdb + "&language=en-US";

        final HashMap<String, String> multiMap = new HashMap<>();

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        JSONArray a = null;
                        try {
                            a = response.getJSONArray("genres");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        assert a != null;
                        for(int i = 0; i<a.length(); i++){

                            JSONObject genres;
                            try {
                                genres = a.getJSONObject(i);
                                multiMap.put(genres.optString("id"), genres.optString("name"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        getMovies(list_order);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ctx, getResources().getString(R.string.network_error_genres), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsObjRequest);

        return multiMap;
    }


    List<Movie> parseJson(JSONArray jsonArray){

        String vote_count, id, vote_average, title, popularity, poster, original_lang, original_title, background,
                adult, overview, release_date;

        List<Movie> movieList = new ArrayList<>();

        for(int i=0; i<jsonArray.length(); i++){
            try {
                JSONObject movie = jsonArray.getJSONObject(i);

                vote_count = movie.optString("vote_count");
                id = movie.optString("id");
                vote_average = movie.optString("vote_average");
                title= movie.optString("title");
                popularity = movie.optString("popularity");
                poster = movie.optString("poster_path");
                original_lang = movie.optString("original_language");
                original_title = movie.optString("original_title");
                background = movie.optString("backdrop_path");
                adult = movie.optString("adult");
                overview = movie.optString("overview");
                release_date = movie.optString("release_date");

                ArrayList<String> genres = new ArrayList<>();
                JSONArray genresList = movie.getJSONArray("genre_ids");
                if (genresList != null) {
                    for (int n=0;n<genresList.length();n++){
                        genres.add(hashMap.get(String.valueOf(genresList.get(n))));
                    }
                }

                movieList.add(new Movie(vote_count, id, vote_average, title, popularity, poster, original_lang, original_title, background, adult,
                        overview, release_date, genres));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return movieList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_scrolling, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
}
