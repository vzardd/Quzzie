package com.cs.quzzie.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cs.quzzie.HomeActivity;
import com.cs.quzzie.MainActivity;
import com.cs.quzzie.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    EditText etEmail,etPwd;
    TextView tvForgot,tvCreateAccount;
    Button btnSignIn;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();
        initViews();
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateCredentials())
                {
                    signIn();
                }
            }
        });
        tvForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,ForgetPasswordActivity.class);
                startActivity(intent);
            }
        });
        tvCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    //On Start
    @Override
    protected void onStart() {
        super.onStart();
        etEmail.setText("");
        etPwd.setText("");
    }

    //signing in
    private void signIn() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait...");
        dialog.show();
        mAuth.signInWithEmailAndPassword(etEmail.getText().toString().trim(),etPwd.getText().toString().trim())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if(mAuth.getCurrentUser().isEmailVerified())
                        {
                            dialog.dismiss();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        else {
                            mAuth.getCurrentUser().sendEmailVerification()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            dialog.dismiss();
                                            Dialog dialog1 = new Dialog(LoginActivity.this);
                                            dialog1.setCancelable(false);
                                            View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.dialog_email_sent,null);
                                            TextView tvOk = view.findViewById(R.id.tvSignUpOk);
                                            tvOk.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    dialog1.dismiss();
                                                    mAuth.signOut();
                                                }
                                            });
                                            dialog1.setContentView(view);
                                            dialog1.show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dialog.dismiss();
                                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateCredentials() {
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
        String email = etEmail.getText().toString().trim();
        if(email.contains(" ") || !email.contains("@") || !email.contains("."))
        {
            etEmail.setError("Email provided is invalid!");
            return false;
        }
        return true;
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPwd = findViewById(R.id.etPass);
        tvForgot = findViewById(R.id.tvForgotPwd);
        tvCreateAccount = findViewById(R.id.tvSignUpActivity);
        btnSignIn = findViewById(R.id.btnSignIn);
        mAuth = FirebaseAuth.getInstance();
    }
}