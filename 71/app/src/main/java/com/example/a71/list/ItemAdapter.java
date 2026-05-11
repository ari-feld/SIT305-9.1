package com.example.a71.list;

import android.view.*;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.a71.R;
import com.example.a71.database.ItemEntity;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onClick(ItemEntity item);
    }

    List<ItemEntity> list;
    OnItemClickListener listener;

    public ItemAdapter(List<ItemEntity> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, category, type;

        public ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.tvName);
            category = v.findViewById(R.id.tvCategory);
            type = v.findViewById(R.id.tvType);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        ItemEntity item = list.get(position);

        holder.name.setText(item.name);
        holder.category.setText("Category: " + item.category);
        holder.type.setText("Type: " + item.type);

        holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(List<ItemEntity> newList) {
        list = newList;
        notifyDataSetChanged();
    }
}