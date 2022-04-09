package com.cs.quzzie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class ChangePassActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    EditText etNew,etConfirm,etOld;
    Button btnChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);
        getSupportActionBar().setTitle("Change Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initViews();
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });
    }

    private void changePassword() {
        if(etOld.getText().toString().trim().isEmpty())
        {
            etOld.setError("This field cannot be empty!");
            return;
        }
        if(etNew.getText().toString().trim().isEmpty())
        {
            etNew.setError("This field cannot be empty!");
            return;
        }
        if(etConfirm.getText().toString().trim().isEmpty())
        {
            etConfirm.setError("This field cannot be empty!");
            return;
        }
        if(!etNew.getText().toString().trim().equals(etConfirm.getText().toString().trim()))
        {
            etConfirm.setError("New password and confirm password should be same!");
            return;
        }
        updatePass();
    }

    private void updatePass() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait...");
        dialog.show();
        AuthCredential credential = EmailAuthProvider.getCredential(mAuth.getCurrentUser().getEmail(),etOld.getText().toString());
        mAuth.getCurrentUser().reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mAuth.getCurrentUser().updatePassword(etNew.getText().toString().trim())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                dialog.dismiss();
                                Toast.makeText(ChangePassActivity.this, "Password changed", Toast.LENGTH_SHORT).show();
                                ChangePassActivity.this.finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(ChangePassActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(ChangePassActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initViews() {
        mAuth = FirebaseAuth.getInstance();
        etNew = findViewById(R.id.etNewPass);
        etConfirm = findViewById(R.id.etConfirmNewPass);
        btnChange = findViewById(R.id.btnChangeNewPass);
        etOld = findViewById(R.id.etOldPass);
    }

    //Options item selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        return true;
    }
}