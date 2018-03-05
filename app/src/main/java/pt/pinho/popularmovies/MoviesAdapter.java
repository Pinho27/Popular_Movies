package pt.pinho.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by luispinho on 23/02/2018.
 */

class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MyViewHolder>{

    List<Movie> movieList;
    Context context;
    int temp;

    public MoviesAdapter(Context context, List<Movie> movieList) {
        this.movieList = movieList;
        this.context = context;
    }

    @Override
    public MoviesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.movie_tile, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onViewDetachedFromWindow(MyViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public void onBindViewHolder(final MoviesAdapter.MyViewHolder holder, int position) {

        final Movie movie = movieList.get(position);

        Picasso.with(context)
                .load("http://image.tmdb.org/t/p/w500" + movie.getPoster())
                .into(holder.movieTile, new Callback() {
                    @Override
                    public void onSuccess() {
                        BitmapDrawable drawable = (BitmapDrawable) holder.movieTile.getDrawable();
                        Bitmap bitmap = drawable.getBitmap();

                        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                            public void onGenerated(Palette palette) {

                                temp = palette.getDarkMutedColor(0);

                                int[] colors = {Color.parseColor("#00FFFFFF"), temp};

                                //create a new gradient color
                                GradientDrawable gd = new GradientDrawable(
                                        GradientDrawable.Orientation.TOP_BOTTOM, colors);
                                gd.setCornerRadius(0.8f);

                                holder.view_space.setBackground(gd);
                            }
                        });

                        holder.tile_title.setText(movie.getTitle());
                        holder.tile_year.setText(movie.getRelease_date().substring(0,4));
                        holder.tile_rating.setText("Rating " + movie.getVote_average() + "/10");

                        setScaleAnimation(holder.itemView);

                    }

                    @Override
                    public void onError() {

                }
        });
    }

    private void setScaleAnimation(View view) {
        ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(700);
        view.startAnimation(anim);
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView movieTile;
        TextView tile_title, tile_year, tile_rating;
        View view_space;


        MyViewHolder(View view) {
            super(view);
            movieTile = view.findViewById(R.id.movie_poster);
            view_space = view.findViewById(R.id.view_space);
            tile_title = view.findViewById(R.id.tile_title);
            tile_year = view.findViewById(R.id.tile_year);
            tile_rating = view.findViewById(R.id.tile_rating);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            Intent intent = new Intent(v.getContext(), ScrollingActivity.class);
            Bundle b = new Bundle();
            intent.putExtra("movie", movieList.get(position));
            b.putInt("dominant_color", temp);
            intent.putExtras(b);
            v.getContext().startActivity(intent);
        }
    }
}
