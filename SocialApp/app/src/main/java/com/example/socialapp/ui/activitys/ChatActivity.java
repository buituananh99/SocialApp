package com.example.socialapp.ui.activitys;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialapp.R;
import com.example.socialapp.adapter.MessageAdapter;
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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.rcy_list_message)
    RecyclerView rcyListMessage;
    @BindView(R.id.edt_message)
    EditText edtMessage;
    @BindView(R.id.img_his_avatar)
    CircleImageView imgHisAvatar;
    @BindView(R.id.tv_his_name)
    TextView tvHisName;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private List<MessageModel> messageList;
    private MessageAdapter adapter;
    private FirebaseUser user;
    private DatabaseReference refChat;

    private String hisEmail, hisAvatar, hisName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        initFirebase();

        hisEmail = getIntent().getStringExtra("USER_EMAIL");
        hisAvatar = getIntent().getStringExtra("USER_AVATAR");
        hisName = getIntent().getStringExtra("USER_NAME");

        Glide.with(this).load(hisAvatar).into(imgHisAvatar);
        tvHisName.setText(hisName);

        getListMessage();

    }

    private void getListMessage() {


        DatabaseReference refUser = FirebaseDatabase.getInstance().getReference("users");

        refUser.orderByChild("email").equalTo(user.getEmail()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    UserModel myUser = ds.getValue(UserModel.class);
                    readMessage(myUser.getAvatar(), myUser.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    // curenImage, receiverUser
    // Shen, bui tuan anh
    void readMessage(String myImage, String myName) {
        rcyListMessage.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();

        refChat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    MessageModel msg = ds.getValue(MessageModel.class);
                    if (msg.getReceiver().equals(user.getEmail()) && msg.getSender().equals(hisEmail)
                            || msg.getReceiver().equals(hisEmail) && msg.getSender().equals(user.getEmail())) {
                        messageList.add(msg);

                    }
                }
                LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                //layoutManager.setReverseLayout(true);
                layoutManager.setStackFromEnd(true);
                rcyListMessage.setLayoutManager(layoutManager);
                adapter = new MessageAdapter(messageList, myImage, hisAvatar, myName, hisName);
                rcyListMessage.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initFirebase() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        refChat = FirebaseDatabase.getInstance().getReference("chats");
    }

    @OnClick(R.id.btn_send)
    public void buttonSendClicked() {
        saveMessage();
    }

    private void saveMessage() {
        String msg = edtMessage.getText().toString();

        if (msg.trim().length() <= 0) {
            edtMessage.setError("bạn chưa nhập nội dung");
            edtMessage.requestFocus();
            return;
        }

        MessageModel message = new MessageModel();

        String id = refChat.push().getKey();

        message.setId(id);
        message.setReceiver(hisEmail);
        message.setSender(user.getEmail());
        message.setMessage(msg);


        refChat.child(id).setValue(message);

        edtMessage.setText("");

    }


}
