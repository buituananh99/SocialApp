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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialapp.InternetConnection;
import com.example.socialapp.R;
import com.example.socialapp.adapter.ChatListAdapter;
import com.example.socialapp.model.MessageModel;
import com.example.socialapp.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ChatListFragment extends Fragment {


    @BindView(R.id.rcy_chat)
    RecyclerView rcyChat;

    private FirebaseUser fUser;
    private DatabaseReference refChats;
    private DatabaseReference refUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        ButterKnife.bind(this, view);

        initFirebase();

        getListMessage();


        return view;
    }



    void initFirebase() {

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        refChats = FirebaseDatabase.getInstance().getReference("chats");
        refUser = FirebaseDatabase.getInstance().getReference("users");
    }

    private List<UserModel> userList;

    private ChatListAdapter adapter;

    private ArrayList<String> emailChatList;
    private String lastMessage;

    void getListMessage() {
        if(!InternetConnection.isConnected(getActivity())){
            InternetConnection.showDialogInternet(getActivity());
            return;
        }
        userList = new ArrayList<>();
        emailChatList = new ArrayList<>();

        // select * from chat where sender = current email
        refChats.orderByChild("sender").equalTo(fUser.getEmail()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()) {
                    refChats.orderByChild("receiver").equalTo(fUser.getEmail()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (!dataSnapshot.exists()) {
                                Toast.makeText(getActivity(), "exists", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            userList.clear();
                            emailChatList.clear();

                            for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                MessageModel message = ds.getValue(MessageModel.class);
                                lastMessage = message.getMessage();
                                emailChatList.add(message.getSender());

                            }
                            Set<String> set = new LinkedHashSet<String>(emailChatList);

                            // Constructing listWithoutDuplicateElements using set
                            List<String> emailUserList = new ArrayList<String>(set);


                            refUser.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    for (DataSnapshot dsUser : dataSnapshot.getChildren()) {

                                        UserModel u = dsUser.getValue(UserModel.class);
                                        for(int i = 0; i < emailUserList.size(); i++){
                                            if(u.getEmail().equals(emailUserList.get(i))){
                                                userList.add(u);
                                            }
                                        }
                                    }
                                    rcyChat.setLayoutManager(new LinearLayoutManager(getActivity()));
                                    adapter = new ChatListAdapter(userList);
                                    rcyChat.setAdapter(adapter);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    return;
                }

                userList.clear();
                emailChatList.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    MessageModel message = ds.getValue(MessageModel.class);
                    lastMessage = message.getMessage();
                    emailChatList.add(message.getReceiver());

                }
                Set<String> set = new LinkedHashSet<String>(emailChatList);

                // Constructing listWithoutDuplicateElements using set
                List<String> emailUserList = new ArrayList<String>(set);


                refUser.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot dsUser : dataSnapshot.getChildren()) {

                            UserModel u = dsUser.getValue(UserModel.class);
                            for(int i = 0; i < emailUserList.size(); i++){
                                if(u.getEmail().equals(emailUserList.get(i))){
                                    userList.add(u);
                                }
                            }
                        }
                        rcyChat.setLayoutManager(new LinearLayoutManager(getActivity()));
                        adapter = new ChatListAdapter(userList);
                        rcyChat.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

}
