package pt.pinho.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ScrollingActivity extends AppCompatActivity {

    ImageView imageView, background, img_trailer, movie_poster;
    TextView title_genres, title_date, desc_overview, vote_average, date_value, genres_value;
    RecyclerView recycler_reviews;
    Context context;
    CardView details_main_info;
    int dominant_color;

    final String apiKeyTmdb = BuildConfig.API_KEY;

    Movie movie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = findViewById(R.id.toolbar);

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        movie = (Movie) getIntent().getSerializableExtra("movie");
        dominant_color = getIntent().getExtras().getInt("dominant_color");
        Log.v("sdfs", String.valueOf(dominant_color));

        //fab.setBackgroundTintList(ColorStateList.valueOf(dominant_color));


        toolbar.setTitle(movie.getTitle());

        imageView = findViewById(R.id.movie_poster_detail);
        background = findViewById(R.id.background);
        movie_poster = findViewById(R.id.movie_poster);

        Picasso.with(this).load("http://image.tmdb.org/t/p/original" + movie.getPoster()).into(background);

        Picasso.with(this).load("http://image.tmdb.org/t/p/w780" + movie.getBackground()).fit().into(imageView);
        setSupportActionBar(toolbar);

        Picasso.with(this).load("http://image.tmdb.org/t/p/w342" + movie.getPoster()).into(movie_poster);


        desc_overview = findViewById(R.id.desc_overview);
        vote_average = findViewById(R.id.vote_average);
        img_trailer = findViewById(R.id.trailer_icon);


        genres_value = findViewById(R.id.genres_value);
        date_value = findViewById(R.id.release_value);

        ArrayList<String> genress = movie.getGenres();
        for(int i=0; i<genress.size();i++)
            genres_value.append(genress.get(i) + ", ");

        date_value.setText(movie.getRelease_date());



        vote_average.setText(movie.getVote_average());
        desc_overview.setText(movie.getOverview());

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler_reviews = findViewById(R.id.recycler_reviews);
        recycler_reviews.setLayoutManager(layoutManager);

        PagerSnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(recycler_reviews);

        context = this;

/*        details_main_info = findViewById(R.id.details_main_info);
        details_main_info.setCardBackgroundColor(Color.TRANSPARENT);
        details_main_info.setCardElevation(0);
        */

        getReviews();
        getTrailer();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    void getReviews(){

        String url = "http://api.themoviedb.org/3/movie/" + movie.getId() + "/reviews?api_key=" + apiKeyTmdb;
        Log.v("sd", url);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.v("dssf", response.toString());

                        JSONArray a = null;
                        try {
                            a = response.getJSONArray("results");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        List<Review> reviewList = parseJson(a);

                        if(!reviewList.isEmpty()){
                            ReviewAdapter reviewAdapter = new ReviewAdapter(context, parseJson(a));
                            recycler_reviews.setAdapter(reviewAdapter);
                            recycler_reviews.setVisibility(View.VISIBLE);
                        }


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

    List<Review> parseJson(JSONArray jsonArray){

        String id, author, content, url;

        List<Review> reviewList = new ArrayList<>();

        for(int i=0; i<jsonArray.length(); i++){

            try {
                JSONObject review = jsonArray.getJSONObject(i);

                id = review.optString("id");
                author = review.optString("author");
                content = review.optString("content");
                url = review.optString("url");

                reviewList.add(new Review(id, author, content, url));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return reviewList;
    }

    void getTrailer(){

        String url = "http://api.themoviedb.org/3/movie/" + movie.getId() + "/videos?api_key=" + apiKeyTmdb;
        Log.v("sd", url);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.v("dssf", response.toString());

                        JSONArray a;
                        try {
                            a = response.getJSONArray("results");
                            JSONObject trailerResponse = a.getJSONObject(0);

                            final String movieTrailerUrl = "http://youtube.com/watch?v=" + trailerResponse.optString("key");

                            img_trailer.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(movieTrailerUrl)));
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

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
}
