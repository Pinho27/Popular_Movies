package pt.pinho.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnticipateInterpolator;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Context context;
    SharedPreferences sharedPreferences;
    String listorder;
    final String apiKeyTmdb = BuildConfig.API_KEY;

    HashMap<String, String> hashMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        recyclerView = findViewById(R.id.recycler_view);
        //recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);

        sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
        listorder = sharedPreferences.getString("list_order", null);
        if(listorder == null){
            sharedPreferences.edit().putString("list_order", "popular").apply();
            listorder = "popular";
        }

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(context);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
        recyclerView.setLayoutManager(layoutManager);

        hashMap = getGenres();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    void getMovies(String type){
        String url = "http://api.themoviedb.org/3/movie/" + type + "?api_key=" + apiKeyTmdb + "&append_to_response=videos";
        Log.v("sd", url);

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

                        MoviesAdapter moviesAdapter = new MoviesAdapter(context, parseJson(a));
                        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(moviesAdapter);
                        alphaAdapter.setDuration(2000);
                        alphaAdapter.setInterpolator(new AnticipateInterpolator());
                        alphaAdapter.setFirstOnly(true);
                        recyclerView.setAdapter(new SlideInBottomAnimationAdapter(alphaAdapter));
                        recyclerView.setVisibility(View.VISIBLE);
                        //progressBar.setVisibility(View.GONE);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsObjRequest);
    }

    HashMap<String, String> getGenres(){

        String url = "https://api.themoviedb.org/3/genre/movie/list?api_key=" + apiKeyTmdb + "&language=en-US";
        Log.v("RUL", url);

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

                        for(int i=0; i<a.length(); i++){

                            JSONObject genres;
                            try {
                                genres = a.getJSONObject(i);
                                multiMap.put(genres.optString("id"), genres.optString("name"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        getMovies(sharedPreferences.getString("list_order", "popular"));

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

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
        inflater.inflate(R.menu.menu_main, menu);

        if(listorder.equals("popular"))
            menu.getItem(0).setChecked(true);
        else
            menu.getItem(1).setChecked(true);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_sort_by_popular:
                if(!item.isChecked()) {
                    item.setChecked(true);
                    sharedPreferences.edit().putString("list_order", "popular").apply();
                    getMovies("popular");
                }


            case R.id.menu_sort_by_rating:
                if(!item.isChecked()) {
                    item.setChecked(true);
                    sharedPreferences.edit().putString("list_order", "top_rated").apply();
                    getMovies("top_rated");
                }

        }
        return super.onOptionsItemSelected(item);

    }
}
