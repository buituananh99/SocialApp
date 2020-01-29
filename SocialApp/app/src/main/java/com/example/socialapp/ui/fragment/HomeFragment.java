package com.example.socialapp.ui.fragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.socialapp.InternetConnection;
import com.example.socialapp.R;
import com.example.socialapp.adapter.ExploreAdapter;
import com.example.socialapp.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

public class HomeFragment extends Fragment {

    @BindView(R.id.view_pager)
    ViewPager2 viewPager;


    private FirebaseAuth auth;
    private DatabaseReference refUser;
    private Dialog dialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ButterKnife.bind(this, view);

        initFirebase();
        setViewPager2();
        return view;
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference("users");
    }



    private void setViewPager2() {

        if(!InternetConnection.isConnected(getActivity())){
            InternetConnection.showDialogInternet(getActivity());
            return;
        }

        dialog = new SpotsDialog(getActivity());
        dialog.setTitle("Loading...");

        List<UserModel> userModelList = new ArrayList<>();

        // TODO: GET USERS
        dialog.show();
        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userModelList.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    UserModel user = ds.getValue(UserModel.class);


                    if(!ds.exists()) return;

                    if (!user.getEmail().equals(auth.getCurrentUser().getEmail())) {

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("favorites").child(user.getId());
                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                                if (!dataSnapshot.child(auth.getCurrentUser().getUid()).exists()) {
                                    userModelList.add(user);
                                    viewPager.setAdapter(new ExploreAdapter(userModelList, viewPager));
                                }

                                dialog.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                dialog.dismiss();
                            }
                        });

                    }

                    dialog.dismiss();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);
        viewPager.setOffscreenPageLimit(3);
        viewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);


        viewPager.setUserInputEnabled(false);


    }
}
