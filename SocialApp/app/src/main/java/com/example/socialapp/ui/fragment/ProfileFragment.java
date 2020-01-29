package com.example.socialapp.ui.fragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialapp.InternetConnection;
import com.example.socialapp.R;
import com.example.socialapp.adapter.PhotoAdapter;
import com.example.socialapp.model.PhotoModel;
import com.example.socialapp.model.UserModel;
import com.example.socialapp.ui.activitys.AddPhotoActivity;
import com.example.socialapp.ui.activitys.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {


    @BindView(R.id.img_avatar)
    ImageView imgAvatar;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_liking)
    TextView tvLiking;
    @BindView(R.id.rcy_photo)
    RecyclerView rcyPhoto;

    private FirebaseUser fUser;
    private DatabaseReference refPhoto;
    private DatabaseReference refUser;
    private List<PhotoModel> photoList;
    private String email, currentUriAvatar;

    private PhotoAdapter adapter;

    // upload image
    private Uri filePath = null;
    private final int PICK_IMAGE_REQUEST = 1;
    private FirebaseStorage storage;
    private StorageReference storageReference;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ButterKnife.bind(this, view);
        initFirebase();

        // check internet
        if (!InternetConnection.isConnected(getActivity())) {
            InternetConnection.showDialogInternet(getActivity());
        }

        getUser();
        getPhoto();

        return view;
    }


    void getPhoto() {


        photoList = new ArrayList<>();
        refPhoto.orderByChild("email").equalTo(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                photoList.clear();
                if (!dataSnapshot.exists()) return;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    PhotoModel photo = ds.getValue(PhotoModel.class);
                    photoList.add(photo);
                }
                rcyPhoto.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                adapter = new PhotoAdapter(photoList);
                rcyPhoto.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast(databaseError.getMessage());
            }
        });
    }

    void initFirebase() {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        refPhoto = FirebaseDatabase.getInstance().getReference("photos");
        refUser = FirebaseDatabase.getInstance().getReference("users");
        email = fUser.getEmail();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


    }



    void getUser() {
        refUser.orderByChild("email").equalTo(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    UserModel user = ds.getValue(UserModel.class);
                    Glide.with(getActivity()).load(user.getAvatar()).into(imgAvatar);
                    tvName.setText(user.getName());
                    currentUriAvatar = user.getAvatar();
                    if (!user.getLiking().equals("")) {
                        tvLiking.setText(user.getLiking());
                    }
                }

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @OnClick(R.id.fab_photo)
    public void fabAddPhotoClicked() {
        startActivity(new Intent(getActivity(), AddPhotoActivity.class));
    }

    @OnClick(R.id.img_log_out)
    public void buttonLogout() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Thông báo");
        builder.setMessage("Bạn có muốn đăng xuất không?");
        builder.setPositiveButton("CÓ", (dialog, which) -> {
            dialog.dismiss();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        });
        builder.setNegativeButton("KHÔNG", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create();
        builder.show();


    }
    CircleImageView imgAvatarUpdate;
    @OnClick(R.id.img_edit)
    public void buttonEditClicked() {
        editProfile();
    }

    private void editProfile() {
        showDialog();
    }

    private void showDialog() {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.diloag_update_user);
        dialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        imgAvatarUpdate = dialog.findViewById(R.id.img_avatar);
        EditText edtName = dialog.findViewById(R.id.edt_name);
        EditText edtLiking = dialog.findViewById(R.id.edt_liking);

        ImageView imgButtonSave = dialog.findViewById(R.id.img_save_update);

        dialog.setCancelable(false);
        // hide dialog update
        dialog.findViewById(R.id.img_back).setOnClickListener(v -> {
            dialog.dismiss();
        });

        imgButtonSave.setOnClickListener(v -> {

            if(edtName.getText().toString().trim().length() < 6){
                edtName.setError("");
                edtName.requestFocus();
                return;
            }

            if(edtLiking.getText().toString().trim().length() < 3){
                edtLiking.setError("");
                edtLiking.requestFocus();
                return;
            }

            updateUser(edtName.getText().toString(), edtLiking.getText().toString(), dialog);
        });

        imgAvatarUpdate.setOnClickListener(v -> {
            chooseImage();
        });

        setUser(imgAvatarUpdate, edtName, edtLiking);

        dialog.create();
        dialog.show();
    }

    void setUser(ImageView imgAvatar, EditText edtName, EditText edtLiking) {
        DatabaseReference refUser = FirebaseDatabase.getInstance().getReference("users");
        refUser.orderByChild("email").equalTo(fUser.getEmail()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dsUser : dataSnapshot.getChildren()) {
                    UserModel user = dsUser.getValue(UserModel.class);
                    Glide.with(getActivity()).load(user.getAvatar()).into(imgAvatar);
                    edtName.setText(user.getName());
                    edtLiking.setText(user.getLiking());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void updateUser(String name, String liking, Dialog dialog) {
        Dialog dialogLoading = new SpotsDialog(getActivity());
        dialogLoading.setTitle("Loading");
        dialogLoading.setCancelable(false);
        dialogLoading.show();
        String id = fUser.getUid();
        if (filePath != null) {
            StorageReference ref = storageReference.child("avatars/" + id);
            ref.putFile(filePath).addOnSuccessListener(taskSnapshot -> {
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    UserModel user = new UserModel();
                    user.setId(id);
                    user.setName(name);
                    user.setEmail(email);
                    user.setAvatar(uri.toString());
                    user.setLiking(liking);
                    refUser.child(fUser.getUid()).setValue(user);
                    dialogLoading.dismiss();
                    dialog.dismiss();
                });
            });
        } else {
            UserModel user = new UserModel();
            user.setId(refUser.push().getKey());
            user.setName(name);
            user.setEmail(email);
            user.setAvatar(currentUriAvatar);
            user.setLiking(liking);
            refUser.child(id).setValue(user);
            dialogLoading.dismiss();
            dialog.dismiss();
        }
    }

    void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                imgAvatarUpdate.setImageBitmap(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
