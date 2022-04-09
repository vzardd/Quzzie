package com.cs.quzzie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AttendActivity extends AppCompatActivity {
    EditText etQuizId;
    Button btnAttend;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attend);
        getSupportActionBar().setTitle("Attend Quiz");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initViews();
        btnAttend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attendIntent();
            }
        });
    }

    private void attendIntent() {
        if(etQuizId.getText().toString().trim().isEmpty())
        {
            return;
        }
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait...");
        dialog.show();
        rootRef.child("Quiz").child(etQuizId.getText().toString().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists())
                {
                    dialog.dismiss();
                    Toast.makeText(AttendActivity.this, "Quiz ID is invalid!", Toast.LENGTH_LONG).show();
                }
                else if((Boolean) snapshot.child("accepting").getValue())
                {
                    rootRef.child("Attempts").child(mAuth.getCurrentUser().getUid()).child(etQuizId.getText().toString().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists())
                            {
                                dialog.dismiss();
                                Toast.makeText(AttendActivity.this, "You've already attended this quiz.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                dialog.dismiss();
                                Intent intent = new Intent(AttendActivity.this,QuizActivity.class);
                                intent.putExtra("id",etQuizId.getText().toString().trim());
                                intent.putExtra("name",(String) snapshot.child("name").getValue());
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            dialog.dismiss();
                            Toast.makeText(AttendActivity.this, "This Quiz is not accepting responses.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else {
                    dialog.dismiss();
                    Toast.makeText(AttendActivity.this, "This Quiz is not accepting responses.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
                Toast.makeText(AttendActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initViews() {
        etQuizId = findViewById(R.id.etQuizID);
        btnAttend = findViewById(R.id.btnAttend);
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