package pt.pinho.popularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    private RecyclerView recycler_reviews, recycler_trailers;
    private Context ctx;

    private final String apiKeyTmdb = BuildConfig.API_KEY;

    Movie movie;
    boolean isFav = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = findViewById(R.id.toolbar);
        final FloatingActionButton fab = findViewById(R.id.fab);

        ctx = this;

        movie = getIntent().getParcelableExtra("movie");

        String[] projections = {"id"};
        String[] selectionArguments = {movie.getId()};

        Cursor cursor = getContentResolver().query(ContentProvider.CONTENT_URI, projections, "id = ?", selectionArguments, null, null);

        assert cursor != null;
        if(cursor.moveToFirst()){
            fab.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_favorite_red_24dp));
            isFav = true;
        }
        cursor.close();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isFav){
                    ContentValues values = new ContentValues();
                    values.put("id", movie.getId());
                    values.put("vote_average", movie.getVote_average());
                    values.put("title", movie.getTitle());
                    values.put("poster", movie.getPoster());
                    values.put("overview", movie.getOverview().replaceAll("'", "\'"));
                    values.put("release_date", movie.getRelease_date());
                    values.put("background", movie.getBackground());

                    if(ContentUris.parseId(getContentResolver().insert(ContentProvider.CONTENT_URI, values)) != -1) {
                        fab.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_favorite_red_24dp));
                        isFav = true;
                    }
                }else {
                    String selection = "id LIKE ?";
                    String[] selectionArgs = { movie.getId() };

                    if(getContentResolver().delete(ContentProvider.CONTENT_URI, selection, selectionArgs) != 0) {
                        fab.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_favorite_border_black_24dp));
                        isFav = false;
                    }
                }
            }
        });

        toolbar.setTitle(movie.getTitle());

        ImageView imageView = findViewById(R.id.movie_poster_detail);
        ImageView background = findViewById(R.id.background);
        ImageView movie_poster = findViewById(R.id.movie_poster);

        Picasso.with(this).load("http://image.tmdb.org/t/p/original" + movie.getPoster()).into(background);

        Picasso.with(this).load("http://image.tmdb.org/t/p/w780" + movie.getBackground()).fit().into(imageView);
        setSupportActionBar(toolbar);

        Picasso.with(this).load("http://image.tmdb.org/t/p/w342" + movie.getPoster()).into(movie_poster);

        TextView desc_overview = findViewById(R.id.desc_overview);
        TextView vote_average = findViewById(R.id.vote_average);
        TextView genres_value = findViewById(R.id.genres_value);
        TextView genres_title = findViewById(R.id.genres_title);
        TextView date_value = findViewById(R.id.release_value);

        if(movie.getGenres() != null){
            ArrayList<String> genress = movie.getGenres();
            for(int i=0; i<genress.size();i++)
                genres_value.append(genress.get(i) + ", ");
        }else {
            genres_title.setVisibility(View.GONE);
            genres_value.setVisibility(View.GONE);
        }

        date_value.setText(movie.getRelease_date());
        vote_average.setText(movie.getVote_average());
        desc_overview.setText(movie.getOverview());


        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler_reviews = findViewById(R.id.recycler_reviews);
        recycler_reviews.setLayoutManager(layoutManager);

        PagerSnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(recycler_reviews);

        LinearLayoutManager layoutManager2
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler_trailers = findViewById(R.id.recycler_trailers);
        recycler_trailers.setLayoutManager(layoutManager2);

        PagerSnapHelper helper2 = new PagerSnapHelper();
        helper2.attachToRecyclerView(recycler_trailers);


        getReviews();
        getTrailers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    void getReviews(){

        String url = "http://api.themoviedb.org/3/movie/" + movie.getId() + "/reviews?api_key=" + apiKeyTmdb;
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

                        List<Review> reviewList = parseJson(a);

                        if(!reviewList.isEmpty()){
                            ReviewAdapter reviewAdapter = new ReviewAdapter(ctx, reviewList);
                            recycler_reviews.setAdapter(reviewAdapter);
                            recycler_reviews.setVisibility(View.VISIBLE);
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ctx, "Error getting reviews!", Toast.LENGTH_SHORT).show();
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

    void getTrailers(){
        final List<Trailer> trailerList = new ArrayList<>();

        String url = "http://api.themoviedb.org/3/movie/" + movie.getId() + "/videos?api_key=" + apiKeyTmdb;
        Log.v("sd", url);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        JSONArray a;
                        try {
                            a = response.getJSONArray("results");

                            for(int i=0; i<a.length();i++){
                                JSONObject trailerResponse = a.getJSONObject(i);
                                trailerList.add(new Trailer(trailerResponse.optString("id"), trailerResponse.optString("key"), trailerResponse.optString("name")));
                            }

                            if(!trailerList.isEmpty()){
                                TrailerAdapter trailerAdapter = new TrailerAdapter(ctx, trailerList);
                                recycler_trailers.setAdapter(trailerAdapter);
                                recycler_trailers.setVisibility(View.VISIBLE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ctx, "Error getting trailer!", Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsObjRequest);
    }
}
