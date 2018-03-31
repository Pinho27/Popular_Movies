package pt.pinho.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {
    final private Context context;
    final private List<Trailer> trailerList;

    TrailerAdapter(Context context, List<Trailer> trailerList) {
        this.context = context;
        this.trailerList = trailerList;
    }

    @Override
    public TrailerAdapter.TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.trailer_item, parent, false);
        return new TrailerAdapter.TrailerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        Trailer trailer = trailerList.get(position);
        Picasso.with(context).load("https://img.youtube.com/vi/"+trailer.getKey()+"/maxresdefault.jpg").into(holder.img_thumb);
        holder.tv_name_trailer.setText(trailer.getName());
    }


    @Override
    public int getItemCount() {
        return trailerList.size();
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView img_thumb;
        TextView tv_name_trailer;
        TrailerViewHolder(View itemView) {
            super(itemView);

            img_thumb = itemView.findViewById(R.id.img_trailer_thumb);
            tv_name_trailer = itemView.findViewById(R.id.tv_trailer_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://youtube.com/watch?v=" + trailerList.get(getAdapterPosition()).getKey())));
        }
    }
}
