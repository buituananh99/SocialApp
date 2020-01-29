package com.example.socialapp.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialapp.R;
import com.example.socialapp.model.FavoriteModel;
import com.example.socialapp.model.UserModel;
import com.example.socialapp.ui.activitys.UserDetailActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.MyViewHolder> {

    List<FavoriteModel> favoriteList;


    public FavoriteAdapter(List<FavoriteModel> favoriteList) {
        this.favoriteList = favoriteList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.setFavorite(favoriteList.get(position));
    }

    @Override
    public int getItemCount() {
        return favoriteList != null ? favoriteList.size() : 0;
    }



    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imgAvatar;
        TextView tvName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvName = itemView.findViewById(R.id.tv_name);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), UserDetailActivity.class);
                intent.putExtra("USER_EMAIL", favoriteList.get(getAdapterPosition()).getEmail());
                itemView.getContext().startActivity(intent);
            });
        }

        void setFavorite(FavoriteModel favorite) {

            DatabaseReference refUser = FirebaseDatabase.getInstance().getReference("users");

            refUser.orderByChild("email").equalTo(favorite.getEmail()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(!dataSnapshot.exists()) return;

                    if(favoriteList.size() <= 0) return;

                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        UserModel user = ds.getValue(UserModel.class);
                        Glide.with(itemView.getContext()).load(user.getAvatar()).into(imgAvatar);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            tvName.setText(favorite.getName());
        }

    }
}
