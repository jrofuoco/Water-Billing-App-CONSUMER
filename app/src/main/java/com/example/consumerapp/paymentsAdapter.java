package com.example.consumerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class paymentsAdapter extends RecyclerView.Adapter<paymentsAdapter.ViewHolder> {

    private ArrayList<paids> paidsList;

    public paymentsAdapter(ArrayList<paids> paidsList) {
        this.paidsList = paidsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_items_payments, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        paids item = paidsList.get(position);
        holder.dateTextView.setText(item.getDate());
        holder.amountTextView.setText(item.getAmount());
    }

    @Override
    public int getItemCount() {
        return paidsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView amountTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.datePayments);
            amountTextView = itemView.findViewById(R.id.payments);
        }
    }
}
