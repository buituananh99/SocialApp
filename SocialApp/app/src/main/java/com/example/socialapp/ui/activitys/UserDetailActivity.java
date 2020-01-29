package com.example.socialapp.ui.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialapp.R;
import com.example.socialapp.adapter.PhotoAdapter;
import com.example.socialapp.model.PhotoModel;
import com.example.socialapp.model.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserDetailActivity extends AppCompatActivity {

    @BindView(R.id.img_avatar)
    ImageView imgAvatar;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_liking)
    TextView tvLiking;
    @BindView(R.id.rcy_photo)
    RecyclerView rcyPhoto;


    private DatabaseReference refPhoto;
    private DatabaseReference refUser;

    private PhotoAdapter adapter;
    private List<PhotoModel> photoList;

    private String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        ButterKnife.bind(this);

        initFirebase();
        getInfoUser();
        getPhotoUser();
    }


    void initFirebase() {
        refPhoto = FirebaseDatabase.getInstance().getReference("photos");
        refUser = FirebaseDatabase.getInstance().getReference("users");
        email = getIntent().getStringExtra("USER_EMAIL");
    }
    UserModel user;
    void getInfoUser() {

        refUser.orderByChild("email").equalTo(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    user = ds.getValue(UserModel.class);
                    Glide.with(getApplicationContext()).load(user.getAvatar()).into(imgAvatar);
                    tvName.setText(user.getName());
                    tvLiking.setText(user.getLiking());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showTost(databaseError.getMessage());
            }
        });


    }

    void showTost(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    PhotoModel photo;
    void getPhotoUser() {
        photoList = new ArrayList<>();
        refPhoto.orderByChild("email").equalTo(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                photoList.clear();
                if (!dataSnapshot.exists()) {

                    return;
                }
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                     photo = ds.getValue(PhotoModel.class);
                    photoList.add(photo);
                }
                rcyPhoto.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
                adapter = new PhotoAdapter(photoList);
                rcyPhoto.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @OnClick({R.id.btn_back,R.id.btn_chat})
    public void onViewClicked(View view) {
        switch (view.getId()){
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_chat:
                Intent intent =new Intent(this, ChatActivity.class);
                intent.putExtra("USER_EMAIL", email);
                intent.putExtra("USER_AVATAR", user.getAvatar());
                intent.putExtra("USER_NAME", user.getName());
                startActivity(intent);
                break;
        }

    }


}
