package com.cs.quzzie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cs.quzzie.model.AttemptsClass;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class HistoryActivity extends AppCompatActivity {
    RecyclerView rvHistory;
    TextView tvEmpty;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getSupportActionBar().setTitle("History");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initViews();
        checkIfEmpty();
    }

    private void checkIfEmpty() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait...");
        dialog.show();
        rootRef.child("Attempts").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getValue()!=null)
                {
                    dialog.dismiss();
                    tvEmpty.setVisibility(View.GONE);
                }
                else {
                    dialog.dismiss();
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<AttemptsClass> options = new FirebaseRecyclerOptions.Builder<AttemptsClass>()
                .setQuery(rootRef.child("Attempts").child(mAuth.getCurrentUser().getUid()),AttemptsClass.class)
                .build();
        FirebaseRecyclerAdapter<AttemptsClass,AttemptsViewHolder> adapter = new FirebaseRecyclerAdapter<AttemptsClass, AttemptsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AttemptsViewHolder holder, int position, @NonNull AttemptsClass model) {
                holder.tvScore.setText(model.getScore()+"/"+model.getTotal());
                rootRef.child("Quiz").child(model.getId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists() && snapshot.getValue()!=null)
                        {
                            holder.tvTitle.setText((String) snapshot.child("name").getValue());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HistoryActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                holder.llRes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(HistoryActivity.this,ResultsActivity.class);
                        intent.putExtra("score",model.getScore());
                        intent.putExtra("total",model.getTotal());
                        intent.putExtra("id",model.getId());
                        intent.putExtra("uid",model.getUid());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public AttemptsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.attempts_row_layout,parent,false);
                return new AttemptsViewHolder(v);
            }
        };
        rvHistory.setAdapter(adapter);
        adapter.startListening();
    }

    public static class AttemptsViewHolder extends RecyclerView.ViewHolder{
        TextView tvTitle,tvScore;
        LinearLayout llRes;

        public AttemptsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAttemptQuiz);
            tvScore = itemView.findViewById(R.id.tvAttemptScore);
            llRes = itemView.findViewById(R.id.llAttemptBox);
        }
    }

    private void initViews() {
        rvHistory = findViewById(R.id.rvHistory);
        tvEmpty = findViewById(R.id.tvAttemptsEmpty);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setHasFixedSize(true);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
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