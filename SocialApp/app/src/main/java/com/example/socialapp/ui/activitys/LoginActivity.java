package com.example.socialapp.ui.activitys;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.socialapp.InternetConnection;
import com.example.socialapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.edt_email)
    EditText edtEmail;
    @BindView(R.id.edt_password)
    EditText edtPassword;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.tv_error)
    TextView tvError;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;


    private FirebaseAuth auth;
    private FirebaseUser fUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        initFirebase();

    }




    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        fUser = auth.getCurrentUser();
    }


    @Override
    protected void onStart() {
        super.onStart();

        if(!InternetConnection.isConnected(this)){
            tvError.setText("Bạn chưa kết nối Internet");
            return;
        }


        if (fUser != null) {
           movingScreenToMain();
        }
    }


    void movingScreenToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }


    @OnClick({R.id.btn_login, R.id.tv_register})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                loginAuth();
                break;
            case R.id.tv_register:
                Intent myIntent = new Intent(this, RegisterActivity.class);
                this.startActivity(myIntent);
                break;
        }
    }

    void showProgressbar() {
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.GONE);
    }

    void hideProgressbar() {
        progressBar.setVisibility(View.GONE);
        btnLogin.setVisibility(View.VISIBLE);
    }

    void loginAuth() {
        String email = edtEmail.getText().toString().trim().toLowerCase();
        String password = edtPassword.getText().toString().trim();

        if(!InternetConnection.isConnected(this)){
            tvError.setText("Bạn chưa kết nối Internet");
            return;
        }

        if(email.trim().length() < 6){
            edtEmail.setError("Nhập email của bạn");
            edtEmail.requestFocus();
            return;
        }

        if(password.length() < 6){
            edtPassword.setError("Nhập password của bạn");
            edtPassword.requestFocus();
            return;
        }


        showProgressbar();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                startActivity(new Intent(this, MainActivity.class));
                hideProgressbar();
            } else {
                tvError.setText(task.getException().getMessage());
                tvError.setVisibility(View.VISIBLE);
                hideProgressbar();
            }
        });
    }


}
