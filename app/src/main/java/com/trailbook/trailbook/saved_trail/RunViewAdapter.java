package com.trailbook.trailbook.saved_trail;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trailbook.trailbook.R;
import com.trailbook.trailbook.database.RunData;
import com.trailbook.trailbook.saved_trail.RunView.OnListFragmentInteractionListener;

import java.util.List;

public class RunViewAdapter extends RecyclerView.Adapter<RunViewAdapter.ViewHolder> {
    private Context mContext;

    private final List<RunData> mValues;
    private final OnListFragmentInteractionListener mListener;

    private int selectedPos = 0;

    RunViewAdapter(List<RunData> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_run_item, parent, false);
        mContext = view.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.itemView.setBackground(selectedPos == position ? getDrawable(R.drawable.selected_border) : getDrawable(R.drawable.entry_border) );

        holder.mItem = mValues.get(position);
        holder.mRunDateView.setText(holder.mItem.getStartTimeDate());
        holder.mRunTimeView.setText(String.valueOf(holder.mItem.getStartTimeOfDay()));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                notifyItemChanged(selectedPos);
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    private Drawable getDrawable(int resourceId) {
        return mContext.getResources().getDrawable(resourceId);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mRunDateView;
        final TextView mRunTimeView;
        public RunData mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mRunDateView = (TextView) view.findViewById(R.id.tv_date);
            mRunTimeView = (TextView) view.findViewById(R.id.tv_time);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mRunDateView.getText() + "'";
        }
    }
}
