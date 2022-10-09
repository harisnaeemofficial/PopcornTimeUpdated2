package com.movieflix.mobile.ui.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import com.movieflix.base.model.video.info.Person;
import com.movieflix.mobile.R;

public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.PersonViewHolder>{

    private List<Person> persons;
    private int width;
    private int height;

    public PeopleAdapter(List<Person> persons){
        this.persons = persons;
    }

    @Override
    public int getItemCount() {
        return persons.size();
    }

    public void setItemSize(Display display, int columnCount, int spacingPixels) {
        final DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        width = (metrics.widthPixels - spacingPixels * (columnCount - 1)) / columnCount;
        height = (int) (width * 1.5f);
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_person, viewGroup, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setItemSize(((WindowManager) viewGroup.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(), viewGroup.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 3 : 5, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, viewGroup.getContext().getResources().getDisplayMetrics()));
        }
        return new PersonViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder personViewHolder, int i) {
        final ViewGroup.LayoutParams params = personViewHolder.cv.getLayoutParams();
        params.width = width;
        params.height = height;
        personViewHolder.cv.setLayoutParams(params);
        personViewHolder.title.setText(persons.get(i).getTitle());
        personViewHolder.subtitle.setText(persons.get(i).getSubtitle());
        String pic = persons.get(i).getProfilePic();
        if (pic == null || pic.replace(" ", "").isEmpty()) {
            personViewHolder.photo.setImageResource(R.drawable.poster);
        } else {
            Picasso.with(personViewHolder.cv.getContext()).load("https://image.tmdb.org/t/p/w185" + persons.get(i).getProfilePic()).placeholder(R.drawable.poster).into(personViewHolder.photo, new Callback() {
                @Override
                public void onSuccess() {
                    personViewHolder.photo.setVisibility(View.VISIBLE);
                    personViewHolder.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError() {
                    personViewHolder.photo.setImageResource(R.drawable.poster);
                }
            });
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    static class PersonViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView title;
        TextView subtitle;
        ImageView photo;
        ProgressBar progressBar;

        PersonViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cv);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            photo = itemView.findViewById(R.id.photo);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

}
