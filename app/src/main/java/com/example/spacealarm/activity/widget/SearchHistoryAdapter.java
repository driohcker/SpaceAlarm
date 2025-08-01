package com.example.spacealarm.activity.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.spacealarm.R;
import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {

    private List<String> mHistoryList;
    private OnHistoryItemClickListener mOnHistoryItemClickListener;
    private OnDeleteClickListener mOnDeleteClickListener;

    public interface OnHistoryItemClickListener {
        void onHistoryItemClick(String keyword);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(String keyword);
    }

    public SearchHistoryAdapter(List<String> historyList) {
        mHistoryList = historyList;
    }

    public void setOnHistoryItemClickListener(OnHistoryItemClickListener listener) {
        mOnHistoryItemClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        mOnDeleteClickListener = listener;
    }

    public OnHistoryItemClickListener getOnHistoryItemClickListener() {
        return mOnHistoryItemClickListener;
    }

    public OnDeleteClickListener getOnDeleteClickListener() {
        return mOnDeleteClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String keyword = mHistoryList.get(position);
        holder.mKeywordTextView.setText(keyword);

        holder.itemView.setOnClickListener(v -> {
            if (mOnHistoryItemClickListener != null) {
                mOnHistoryItemClickListener.onHistoryItemClick(keyword);
            }
        });

        holder.mDeleteImageView.setOnClickListener(v -> {
            if (mOnDeleteClickListener != null) {
                mOnDeleteClickListener.onDeleteClick(keyword);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mHistoryList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mKeywordTextView;
        ImageView mDeleteImageView;

        ViewHolder(View itemView) {
            super(itemView);
            mKeywordTextView = itemView.findViewById(R.id.history_keyword);
            mDeleteImageView = itemView.findViewById(R.id.delete_history);
        }
    }
}