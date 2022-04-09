package com.cs.quzzie.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cs.quzzie.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    EditText etFullName,etEmail,etPwd,etConfirmPwd;
    Button btnSignUp;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initViews();
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate())
                {
                    signUp();
                }
            }
        });
    }

    private void signUp() {
        String email = etEmail.getText().toString().trim();
        String pass = etConfirmPwd.getText().toString().trim();
        String fullname = etFullName.getText().toString().trim();
        ProgressDialog builder = new ProgressDialog(this);
        builder.setMessage("Please wait...");
        builder.setCancelable(false);
        builder.show();
        mAuth.createUserWithEmailAndPassword(email,pass)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        mAuth.signInWithEmailAndPassword(email,pass)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        mAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                builder.dismiss();
                                                HashMap<String,Object> usr = new HashMap<String, Object>();
                                                usr.put("uid",mAuth.getCurrentUser().getUid());
                                                usr.put("name",fullname);
                                                rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(usr);
                                                Dialog dialog = new Dialog(SignUpActivity.this);
                                                dialog.setCancelable(false);
                                                View view = LayoutInflater.from(SignUpActivity.this).inflate(R.layout.dialog_email_sent,null);
                                                TextView tvOk = view.findViewById(R.id.tvSignUpOk);
                                                tvOk.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        dialog.dismiss();
                                                        mAuth.signOut();
                                                        SignUpActivity.this.finish();
                                                    }
                                                });
                                                dialog.setContentView(view);
                                                dialog.show();
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        builder.dismiss();
                                        Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        builder.dismiss();
                    }
                });

    }

    private boolean validate() {
        if(etFullName.getText().toString().trim().isEmpty())
        {
            etFullName.setError("Full name can't be empty!");
            return false;
        }
        if(etEmail.getText().toString().trim().isEmpty())
        {
            etEmail.setError("Email can't be empty!");
            return false;
        }
        if(etPwd.getText().toString().trim().isEmpty())
        {
            etPwd.setError("Password can't be empty!");
            return false;
        }
        if(etConfirmPwd.getText().toString().trim().isEmpty())
        {
            etConfirmPwd.setError("Confirm Password can't be empty!");
            return false;
        }
        String email = etEmail.getText().toString().trim();
        if(email.contains(" ") || !email.contains("@") || !email.contains("."))
        {
            etEmail.setError("Email provided is invalid!");
            return false;
        }

        return true;
    }

    //Initializing views
    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etSignUpEmail);
        etPwd = findViewById(R.id.etSignUpPwd);
        etConfirmPwd = findViewById(R.id.etConfirmPwd);
        btnSignUp = findViewById(R.id.btnSignUp);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    //Options Menu Item click

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return true;
    }
}