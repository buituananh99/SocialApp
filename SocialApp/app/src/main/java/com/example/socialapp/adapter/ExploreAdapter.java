package com.example.socialapp.adapter;

import android.content.Intent;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.socialapp.R;
import com.example.socialapp.model.FavoriteModel;
import com.example.socialapp.model.PhotoModel;
import com.example.socialapp.model.UserModel;
import com.example.socialapp.ui.activitys.UserDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.ExploreViewHolder> {

    private List<UserModel> userModelList;
    private ViewPager2 viewPager2;

    private FirebaseAuth fUser;
    private DatabaseReference refFavorite;

    public ExploreAdapter(List<UserModel> userModelList, ViewPager2 viewPager2) {
        this.userModelList = userModelList;
        this.viewPager2 = viewPager2;

        fUser = FirebaseAuth.getInstance();
        refFavorite = FirebaseDatabase.getInstance().getReference("favorites");

    }

    @NonNull
    @Override
    public ExploreAdapter.ExploreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_explore, parent, false);


        return new ExploreViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ExploreAdapter.ExploreViewHolder holder, int position) {

        UserModel user = userModelList.get(position);
        holder.setExplore(user);


        // TODO: set animation item view
        setScaleAnimation(holder.itemView);


    }

    @Override
    public int getItemCount() {
        return userModelList == null ? 0 : userModelList.size();
    }

    class ExploreViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvLiking;
        LinearLayout imgFavorite, imgClose;
        ImageView imgAvatar;

        public ExploreViewHolder(@NonNull View itemView) {
            super(itemView);


            imgAvatar = itemView.findViewById(R.id.img_avatar);
            imgFavorite = itemView.findViewById(R.id.img_favorite);
            imgClose = itemView.findViewById(R.id.img_close);
            tvName = itemView.findViewById(R.id.tv_name);
            tvLiking = itemView.findViewById(R.id.tv_liking);

            imgFavorite.setOnClickListener(v -> {

                UserModel user = userModelList.get(getAdapterPosition());

                DatabaseReference fuser = FirebaseDatabase.getInstance().getReference("users");

                fuser.orderByChild("email").equalTo(fUser.getCurrentUser().getEmail()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            UserModel userModel = ds.getValue(UserModel.class);
                            saveFavorite(user.getId(), userModel.getName(), user.getName(), user.getEmail(), user.getAvatar(), getAdapterPosition());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            });

            imgClose.setOnClickListener(v -> {
                removeAt(getAdapterPosition());
            });

            imgAvatar.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), UserDetailActivity.class);
                intent.putExtra("USER_EMAIL", userModelList.get(getAdapterPosition()).getEmail());
                itemView.getContext().startActivity(intent);
            });


        }

        public void setExplore(UserModel exploreModel) {


            Glide.with(itemView.getContext()).load(exploreModel.getAvatar()).into(imgAvatar);
            tvName.setText(exploreModel.getName());
            tvLiking.setText(exploreModel.getLiking());

        }

    }


    void saveFavorite(String userId, String userFavorite, String name, String email, String avatar, int pos) {

        FavoriteModel favorite = new FavoriteModel();

        favorite.setId(refFavorite.push().getKey());
        favorite.setName(name);
        favorite.setEmail(email);

        refFavorite.child(name).child(userFavorite).setValue(favorite);

        removeAt(pos);
    }

    void removeAt(int position) {
        userModelList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, userModelList.size());
    }


    void setScaleAnimation(View view) {
        ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(500);
        view.startAnimation(anim);
    }


}
