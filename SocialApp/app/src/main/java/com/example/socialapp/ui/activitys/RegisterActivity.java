package com.example.socialapp.ui.activitys;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.socialapp.InternetConnection;
import com.example.socialapp.R;
import com.example.socialapp.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.edt_name)
    EditText edtName;
    @BindView(R.id.edt_email)
    EditText edtEmail;
    @BindView(R.id.edt_password)
    EditText edtPassword;
    @BindView(R.id.btn_Register)
    Button btnRegister;
    @BindView(R.id.img_avatar)
    ImageView imgAvatar;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.tv_error)
    TextView tvError;
    @BindView(R.id.tv_question_account)
    TextView tvQuestionAccount;
    @BindView(R.id.tv_login)
    TextView tvLogin;

    // upload image
    private Uri filePath = null;
    private final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        initFirebase();

    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    @OnClick({R.id.btn_Register, R.id.tv_login, R.id.img_avatar})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_Register:
                createAccountAuth();
                break;
            case R.id.tv_login:
                finish();
                break;
            case R.id.img_avatar:
                chooseImage();
                break;
        }
    }

    void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void createAccountAuth() {

        if (!InternetConnection.isConnected(this)) {
            tvError.setText("Bạn chưa kết nối Internet");
            return;
        }

        //TODO: VALIDATION
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim().toLowerCase();
        String password = edtPassword.getText().toString().trim();

        if (name.trim().length() < 6) {
            edtName.setError("Nhập tên của bạn");
            edtName.requestFocus();
            return;
        }

        if (email.trim().length() < 6) {
            edtEmail.setError("Nhập email của bạn");
            edtEmail.requestFocus();
            return;
        }

        if (password.length() < 6) {
            edtPassword.setError("Nhập password của bạn");
            edtPassword.requestFocus();
            return;
        }

        if (filePath == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        // create auth
        showProgressbar();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                createUserDB(name, email);
            } else {
                tvError.setText(task.getException().getMessage());
                hideProgressbar();
            }
        });


    }

    void showProgressbar() {
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setVisibility(View.GONE);
    }

    void hideProgressbar() {
        progressBar.setVisibility(View.GONE);
        btnRegister.setVisibility(View.VISIBLE);
    }

    void createUserDB(String name, String email) {

        if (filePath == null) {

            UserModel user = new UserModel();
            user.setId(auth.getUid());
            user.setName(name);
            user.setEmail(email);
            user.setLiking("");
            myRef.child(user.getId()).setValue(user);

            movingScreenToMain();

        } else {
            uploadImage(auth.getUid(), name, email);
        }


    }

    void movingScreenToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imgAvatar.setImageBitmap(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    void uploadImage(String uid, String name, String email) {
        if (filePath != null) {

            StorageReference ref = storageReference.child("avatars/" + uid);
            ref.putFile(filePath).addOnSuccessListener(taskSnapshot -> {
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    UserModel user = new UserModel();
                    user.setId(uid);
                    user.setName(name);
                    user.setEmail(email);
                    user.setAvatar(uri.toString());
                    user.setLiking("");
                    myRef.child(uid).setValue(user);
                    movingScreenToMain();
                });
            });
        }
    }

}
