package com.dream.lemon.hackathon.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dream.lemon.hackathon.R;
import com.google.android.gms.location.places.Place;

import java.util.List;

/**
 * Created by jalcantara on 27/10/2017.
 */

public class TempAdapter extends RecyclerView.Adapter<TempAdapter.TempViewHolder> {

    private List<Place> data;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Place item);
    }

    public static class TempViewHolder extends RecyclerView.ViewHolder {
        public TextView place;

        public TempViewHolder(View v) {
            super(v);
            place = (TextView) v.findViewById(R.id.TempAdapter);
        }

        public void bind(final Place item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    public TempAdapter(List<Place> data, OnItemClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public TempViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.temp_cell_layout, viewGroup, false);
        v.getBackground().setAlpha(100);
        return new TempViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TempViewHolder viewHolder, int i) {
        viewHolder.place.setText(data.get(i).getName());
        viewHolder.bind(data.get(i), listener);
    }

}
