package com.example.simplerestcachingstrategy.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.simplerestcachingstrategy.R;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
    private Context context;
    private List<Employee> list;
    public RecyclerAdapter(Context context, List<Employee> list){
        this.context = context;
        this.list = list;
    }
    public void setList( List<Employee> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item_adapter, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.MyViewHolder holder, int position) {
        holder.textView1.setText(list.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return (list !=null? list.size():0);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView1;
        TextView textView1;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView1 = (ImageView) itemView.findViewById(R.id.image);
            textView1 = (TextView) itemView.findViewById(R.id.title);
        }
    }
}
