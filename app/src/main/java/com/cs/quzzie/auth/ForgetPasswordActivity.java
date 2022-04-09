package com.cs.quzzie.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cs.quzzie.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {

    EditText etEmail;
    Button btnReset;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setTitle("Forget Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initViews();
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetEmail();
            }
        });
    }

    private void resetEmail() {

        if(etEmail.getText().toString().trim().isEmpty())
        {
            etEmail.setError("Enter your email!");
            return;
        }
        ProgressDialog progressDialog = new ProgressDialog(ForgetPasswordActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        Dialog dialog = new Dialog(ForgetPasswordActivity.this);
        dialog.setCancelable(false);
        View v = LayoutInflater.from(ForgetPasswordActivity.this).inflate(R.layout.dialog_email_sent,null);
        TextView tvOk = v.findViewById(R.id.tvSignUpOk);
        TextView tvTitle = v.findViewById(R.id.textView2);
        TextView tvMessage = v.findViewById(R.id.textView3);
        tvTitle.setText("Password Reset!");
        tvMessage.setText("A Password reset link is sent to your email. Click on the link and change your password.");
        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                ForgetPasswordActivity.this.finish();
            }
        });
        dialog.setContentView(v);
        mAuth.sendPasswordResetEmail(etEmail.getText().toString().trim())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        dialog.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ForgetPasswordActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void initViews() {
        etEmail = findViewById(R.id.etForgetMail);
        btnReset = findViewById(R.id.btnGetForget);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        return true;
    }
}