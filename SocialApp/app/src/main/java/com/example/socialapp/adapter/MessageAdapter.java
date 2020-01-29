package com.example.socialapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialapp.R;
import com.example.socialapp.model.MessageModel;
import com.example.socialapp.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private List<MessageModel> messageList;
    private String imageSender;
    private String imageReceiver;
    private String nameSender;
    private String nameReceiver;

    public MessageAdapter(List<MessageModel> messageList, String imageSender, String imageReceiver, String nameSender, String nameReceiver) {
        this.messageList = messageList;

        this.imageSender = imageSender;
        this.imageReceiver = imageReceiver;

        this.nameSender = nameSender;
        this.nameReceiver = nameReceiver;

        fUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public MessageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = null;
        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right, parent, false);
        } else if (viewType == MSG_TYPE_LEFT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left, parent, false);

        }

        return new MessageAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MyViewHolder holder, int position) {
        holder.setListChat(messageList.get(position));
    }

    private FirebaseUser fUser;

    @Override
    public int getItemViewType(int position) {


        if (fUser.getEmail().equals(messageList.get(position).getSender())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }

    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imgAvatar;
        TextView tvMsg;
        TextView tvName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvMsg = itemView.findViewById(R.id.tv_message);
            tvName = itemView.findViewById(R.id.tv_name);
        }

        void setListChat(MessageModel message) {
            if (fUser.getEmail().equals(message.getSender())) {
                Glide.with(itemView.getContext()).load(imageSender).into(imgAvatar);
                tvName.setText(nameSender);
            } else {
                Glide.with(itemView.getContext()).load(imageReceiver).into(imgAvatar);
                tvName.setText(nameReceiver);
            }

            tvMsg.setText(message.getMessage());
        }

    }
}
