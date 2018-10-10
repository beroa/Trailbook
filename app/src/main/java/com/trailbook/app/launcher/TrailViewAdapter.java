package com.trailbook.app.launcher;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trailbook.app.R;
import com.trailbook.app.database.AppDatabase;
import com.trailbook.app.database.RunDao;
import com.trailbook.app.database.RunData;
import com.trailbook.app.database.TrailData;
import com.trailbook.app.launcher.TrailView.OnListFragmentInteractionListener;


import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link TrailData} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class TrailViewAdapter extends RecyclerView.Adapter<TrailViewAdapter.ViewHolder> {
    private RunDao runDao;

    private RunData rundata;
    private final List<TrailData> mTrailList;
    private final OnListFragmentInteractionListener mListener;

    TrailViewAdapter(List<TrailData> trailList, OnListFragmentInteractionListener listener) {
        mTrailList = trailList;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_trail_item, parent, false);
        Context mContext = view.getContext();
        runDao = AppDatabase.getAppDatabase(mContext).runDao();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = mTrailList.get(position);
        holder.mTrailNameView.setText(holder.mItem.getTrailName());

        String trailId = holder.mItem.getId();
        holder.mTrailDateView.setText( runDao.getRecentByTrailId(trailId).getStartTimeDate());
        holder.mTrailDurationView.setText(runDao.getRecentByTrailId(trailId).getDurationString());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTrailList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mTrailNameView;
        final TextView mTrailDateView;
        final TextView mTrailDurationView;
        public TrailData mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mTrailNameView = (TextView) view.findViewById(R.id.tv_trail_list_name);
            mTrailDateView = (TextView) view.findViewById(R.id.tv_trail_list_date);
            mTrailDurationView = (TextView) view.findViewById(R.id.tv_trail_list_duration);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTrailDateView.getText() + "'";
        }
    }
}
