package com.example.socialapp.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialapp.InternetConnection;
import com.example.socialapp.R;
import com.example.socialapp.adapter.FavoriteAdapter;
import com.example.socialapp.model.FavoriteModel;
import com.example.socialapp.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FavoriteFragment extends Fragment {


    @BindView(R.id.rcy_favorite)
    RecyclerView rcyFavorite;

    private FavoriteAdapter adapter;
    private List<FavoriteModel> favoriteList;

    private FirebaseUser fUser;
    private DatabaseReference refFavorite;
    String name;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        ButterKnife.bind(this, view);
        initFirebse();
        DatabaseReference refUser = FirebaseDatabase.getInstance().getReference("users");
        refUser.orderByChild("email").equalTo(fUser.getEmail()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    UserModel userModel = ds.getValue(UserModel.class);
                    name = userModel.getName();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        getFavorite();

        return view;
    }

    private void initFirebse() {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        refFavorite = FirebaseDatabase.getInstance().getReference("favorites");
    }

    void getFavorite() {

        if(!InternetConnection.isConnected(getActivity())){
            InternetConnection.showDialogInternet(getActivity());
            return;
        }

        favoriteList = new ArrayList<>();
        refFavorite.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if(!dataSnapshot.exists()){
                    return;
                }
                favoriteList.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    for(DataSnapshot ds1: ds.getChildren()){
                        FavoriteModel favorite = ds1.getValue(FavoriteModel.class);
                        if(ds1.getKey().equals(name)){
                            favoriteList.add(favorite);
                        }
                    }
                }

                adapter = new FavoriteAdapter(favoriteList);
                rcyFavorite.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                rcyFavorite.setHasFixedSize(true);
                rcyFavorite.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast(databaseError.getMessage());
            }
        });
    }



    void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

}
