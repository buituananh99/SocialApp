package com.example.socialapp.ui.activitys;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.socialapp.R;
import com.example.socialapp.model.PhotoModel;
import com.example.socialapp.model.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class AddPhotoActivity extends AppCompatActivity {

    @BindView(R.id.img_photo)
    ImageView imgPhoto;

    private FirebaseAuth auth;
    private DatabaseReference refPhoto;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private Uri filePath = null;
    private final int PICK_IMAGE_REQUEST = 1;

    private String id, email;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);
        ButterKnife.bind(this);

        initFirebase();

        id = auth.getUid();
        email = auth.getCurrentUser().getEmail();


        dialog = new SpotsDialog(this);
    }

    void initFirebase() {
        auth = FirebaseAuth.getInstance();
        refPhoto = FirebaseDatabase.getInstance().getReference("photos");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    @OnClick({R.id.btn_close, R.id.btn_add, R.id.btn_choose_photo})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_close:
                finish();
                break;
            case R.id.btn_add:
                savePhoto();
                break;
            case R.id.btn_choose_photo:
                choosePhoto();
                break;
        }
    }

    void choosePhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    void savePhoto() {
        if (filePath != null) {
            dialog.setTitle("Loading..");
            dialog.show();
            StorageReference ref = storageReference.child("photo/" + auth);
            ref.putFile(filePath).addOnSuccessListener(taskSnapshot -> {
                ref.getDownloadUrl().addOnSuccessListener(uri -> {

                    PhotoModel photo = new PhotoModel();
                    photo.setId(refPhoto.push().getKey());
                    photo.setPhoto(uri.toString());
                    photo.setEmail(email);
                    refPhoto.child(refPhoto.push().getKey()).setValue(photo);
                    dialog.dismiss();
                    finish();
                });
            }).addOnFailureListener(e -> {
                dialog.dismiss();
                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imgPhoto.setImageBitmap(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
