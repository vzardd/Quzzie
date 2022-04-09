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

public class LeaderboardActivity extends AppCompatActivity {
    RecyclerView rvRank;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    String quizId;
    TextView tvRankEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        getSupportActionBar().setTitle("Leaderboard");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setElevation(0);
        initViews();
        checkIfEmpty();
    }

    private void checkIfEmpty() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait...");
        dialog.show();
        rootRef.child("Ranking").child(quizId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dialog.dismiss();
                if(snapshot.exists() && snapshot.getValue()!=null)
                {
                    tvRankEmpty.setVisibility(View.GONE);
                }
                else {
                    tvRankEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(LeaderboardActivity.this, "2", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
                tvRankEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(LeaderboardActivity.this, "3", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<AttemptsClass> options = new FirebaseRecyclerOptions.Builder<AttemptsClass>()
                .setQuery(rootRef.child("Ranking").child(quizId).orderByChild("score"),AttemptsClass.class)
                .build();
        FirebaseRecyclerAdapter<AttemptsClass,RankViewHolder> adapter = new FirebaseRecyclerAdapter<AttemptsClass, RankViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RankViewHolder holder, int position, @NonNull AttemptsClass model) {
                holder.tvRank.setText(String.valueOf(position+1));
                holder.tvScore.setText(model.getScore()+"/"+model.getTotal());
                rootRef.child("Users").child(model.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists() && snapshot.getValue()!=null)
                        {
                            holder.tvName.setText((String) snapshot.child("name").getValue());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                holder.llRankBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        rootRef.child("Manage").child(mAuth.getCurrentUser().getUid()).child(quizId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists() && snapshot.getValue()!=null)
                                        {
                                            Intent intent = new Intent(LeaderboardActivity.this,ResultsActivity.class);
                                            intent.putExtra("score",model.getScore());
                                            intent.putExtra("total",model.getTotal());
                                            intent.putExtra("id",quizId);
                                            intent.putExtra("uid",model.getUid());
                                            startActivity(intent);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }
                });
            }

            @NonNull
            @Override
            public RankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rank_row_layout,parent,false);
                return new RankViewHolder(v);
            }

            @NonNull
            @Override
            public AttemptsClass getItem(int position) {
                return super.getItem(getItemCount() -1 -position);
            }
        };
        rvRank.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RankViewHolder extends RecyclerView.ViewHolder{
        TextView tvRank,tvName,tvScore;
        LinearLayout llRankBox;
        public RankViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvName = itemView.findViewById(R.id.tvRankUsername);
            tvScore = itemView.findViewById(R.id.tvRankScore);
            llRankBox = itemView.findViewById(R.id.llRankBox);
        }
    }
    private void initViews() {
        rvRank = findViewById(R.id.rvRank);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvRank.setLayoutManager(llm);
        rvRank.setHasFixedSize(true);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        quizId = getIntent().getStringExtra("id");
        tvRankEmpty = findViewById(R.id.tvRankEmpty);
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