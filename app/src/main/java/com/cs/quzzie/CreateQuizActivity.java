package com.cs.quzzie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class CreateQuizActivity extends AppCompatActivity {
    EditText etQuizTitle;
    Button btnCreate;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);
        getSupportActionBar().setTitle("Create Quiz");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initViews();
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etQuizTitle.getText().toString().trim().isEmpty())
                {
                    etQuizTitle.setError("Quiz Title cannot be empty!");
                    return;
                }
                createQuiz();
            }
        });
    }


    private void createQuiz() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait...");
        dialog.show();
        HashMap<String,Object> quiz = new HashMap<String, Object>();
        String key = rootRef.child("Quiz").push().getKey();
        quiz.put("name",etQuizTitle.getText().toString().trim());
        quiz.put("id",key);
        quiz.put("timestamp", ServerValue.TIMESTAMP);
        quiz.put("accepting",true);
        rootRef.child("Quiz").child(key).setValue(quiz)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        rootRef.child("Manage").child(mAuth.getCurrentUser().getUid()).child(key).child("id").setValue(key)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        dialog.dismiss();
                                        Toast.makeText(CreateQuizActivity.this, "Quiz Created successfully!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(CreateQuizActivity.this,CreateQuestionActivity.class);
                                        intent.putExtra("name",etQuizTitle.getText().toString().trim());
                                        intent.putExtra("key",key);
                                        intent.putExtra("accepting",true);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.dismiss();
                                        Toast.makeText(CreateQuizActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(CreateQuizActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void initViews() {
        etQuizTitle = findViewById(R.id.etQuizTitle);
        btnCreate = findViewById(R.id.btnCreateQuiz);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
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