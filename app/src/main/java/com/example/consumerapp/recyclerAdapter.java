package com.example.consumerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class recyclerAdapter extends RecyclerView.Adapter<recyclerAdapter.MyViewHolder> {
    private ArrayList<Bills> billsList;


    public recyclerAdapter(ArrayList<Bills> billsList) {
        this.billsList = billsList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView billTxt;
        TextView amountTxt;

        public MyViewHolder(final View view) {
            super(view);
            billTxt = view.findViewById(R.id.textView5);
            amountTxt = view.findViewById(R.id.textView6);
        }
    }

    @NonNull
    @Override
    public recyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_items, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull recyclerAdapter.MyViewHolder holder, int position) {
        // Get the current bill object
        Bills bill = billsList.get(position);

        // Set the reading date to textView5
        holder.billTxt.setText(bill.getReadingLastMonthDate());

        // Set the amount payable to textView6
        holder.amountTxt.setText(String.format("%.2f", bill.getAmountPayable()));
    }


    @Override
    public int getItemCount() {
        return billsList.size();
    }
}
