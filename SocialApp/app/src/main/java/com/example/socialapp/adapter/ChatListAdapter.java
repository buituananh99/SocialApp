package com.example.socialapp.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialapp.R;
import com.example.socialapp.model.MessageModel;
import com.example.socialapp.model.UserModel;
import com.example.socialapp.ui.activitys.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.MyViewHolder> {

    private List<UserModel> userList;
    private DatabaseReference refChat;

    public ChatListAdapter(List<UserModel> userList) {
        this.userList = userList;

    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_chat, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.setUserChatList(userList.get(position));
        lastMessage(position, holder.tvLastMessage);


    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    private String lastMessage;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imgAvatar;
        TextView tvName, tvLastMessage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), ChatActivity.class);
                UserModel user = userList.get(getAdapterPosition());
                intent.putExtra("USER_EMAIL", user.getEmail());
                intent.putExtra("USER_AVATAR", user.getAvatar());
                intent.putExtra("USER_NAME", user.getName());
                itemView.getContext().startActivity(intent);
            });

        }

        void setUserChatList(UserModel user) {
            Glide.with(itemView.getContext()).load(user.getAvatar()).into(imgAvatar);
            tvName.setText(user.getName());
        }


    }


    void lastMessage(int pos, TextView tvMessage) {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        refChat = FirebaseDatabase.getInstance().getReference("chats");
        refChat.orderByChild("sender").equalTo(fUser.getEmail()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()) {
                    return;
                }

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    MessageModel msg = ds.getValue(MessageModel.class);
                    if (userList.size() == 0) return;

                    if (msg.getReceiver().equals(userList.get(pos).getEmail())) {
                        lastMessage = msg.getMessage();
                    }
                }
                tvMessage.setText(lastMessage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
