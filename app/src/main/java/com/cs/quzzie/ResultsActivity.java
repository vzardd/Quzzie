package com.cs.quzzie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cs.quzzie.model.ResultQnClass;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ResultsActivity extends AppCompatActivity {
    TextView tvScore, tvPercentage;
    ProgressBar pbProgress;
    RecyclerView rvRes;
    int score,total,per;
    String quizId,uid;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        getSupportActionBar().setTitle("Result");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setElevation(0);
        initViews();
        setScore();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<ResultQnClass> options = new FirebaseRecyclerOptions.Builder<ResultQnClass>()
                .setQuery(rootRef.child("Results").child(quizId).child(uid),ResultQnClass.class)
                .build();
        FirebaseRecyclerAdapter<ResultQnClass,ResViewHolder> adapter = new FirebaseRecyclerAdapter<ResultQnClass, ResViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ResViewHolder holder, int position, @NonNull ResultQnClass model) {
                setView(holder,position,model);
                setRes(holder,position,model);
                holder.op1.setEnabled(false);
                holder.op2.setEnabled(false);
                holder.op3.setEnabled(false);
                holder.op4.setEnabled(false);
                storageRef.child("Questions").child(quizId).child(model.getQid()+".jpg").getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if(uri!=null)
                                {
                                    holder.ivRef.setVisibility(View.VISIBLE);
                                    Glide.with(ResultsActivity.this).load(uri.toString()).into(holder.ivRef);
                                }
                                else {
                                    holder.ivRef.setVisibility(View.GONE);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                holder.ivRef.setVisibility(View.GONE);
                            }
                        });
            }

            @NonNull
            @Override
            public ResViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_row_layout,parent,false);
                return new ResViewHolder(v);
            }
        };
        rvRes.setAdapter(adapter);
        adapter.startListening();
    }

    private void setRes(ResViewHolder holder, int position, ResultQnClass model) {
        switch (model.getMarked())
        {
            case 1:
                if(model.isResult())
                {
                    holder.op1.setBackground(getResources().getDrawable(R.drawable.correct_rb_bg));
                    holder.op1.setChecked(true);
                }
                else {
                    holder.op1.setBackground(getResources().getDrawable(R.drawable.wrong_rb_bg));
                    holder.op1.setChecked(true);
                }
                break;
            case 2:
                if(model.isResult())
                {
                    holder.op2.setBackground(getResources().getDrawable(R.drawable.correct_rb_bg));
                    holder.op2.setChecked(true);
                }
                else {
                    holder.op2.setBackground(getResources().getDrawable(R.drawable.wrong_rb_bg));
                    holder.op2.setChecked(true);
                }
                break;
            case 3:
                if(model.isResult())
                {
                    holder.op3.setBackground(getResources().getDrawable(R.drawable.correct_rb_bg));
                    holder.op3.setChecked(true);
                }
                else {
                    holder.op3.setBackground(getResources().getDrawable(R.drawable.wrong_rb_bg));
                    holder.op3.setChecked(true);
                }
                break;
            case 4:
                if(model.isResult())
                {
                    holder.op4.setBackground(getResources().getDrawable(R.drawable.correct_rb_bg));
                    holder.op4.setChecked(true);
                }
                else {
                    holder.op4.setBackground(getResources().getDrawable(R.drawable.wrong_rb_bg));
                    holder.op4.setChecked(true);
                }
                break;
        }
    }

    private void setView(ResViewHolder holder, int position, ResultQnClass model) {
        holder.tvQnNo.setText("Question "+String.valueOf(position+1)+" :");
        if(model.isResult())
        {
            holder.tvPoint.setText("1 point");
        }
        else {
            holder.tvPoint.setText("0 point");
        }
        rootRef.child("Questions").child(quizId).child(model.getQid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            holder.tvQn.setText((String) snapshot.child("question").getValue());
                            holder.op1.setText((String) snapshot.child("op1").getValue());
                            holder.op2.setText((String) snapshot.child("op2").getValue());
                            String op3 = (String) snapshot.child("op3").getValue();
                            if(op3.length()>0)
                            {
                                holder.op3.setVisibility(View.VISIBLE);
                                holder.op3.setText(op3);
                            }
                            else{
                                holder.op3.setVisibility(View.GONE);
                            }
                            String op4 = (String) snapshot.child("op4").getValue();
                            if(op4.length()>0)
                            {
                                holder.op4.setVisibility(View.VISIBLE);
                                holder.op4.setText(op3);
                            }
                            else{
                                holder.op4.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ResultsActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public static class ResViewHolder extends RecyclerView.ViewHolder{
        TextView tvQnNo,tvQn,tvPoint;
        ImageView ivRef;
        RadioButton op1,op2,op3,op4;
        public ResViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQnNo = itemView.findViewById(R.id.tvResQnNo);
            tvQn = itemView.findViewById(R.id.tvResDisplayQn);
            ivRef = itemView.findViewById(R.id.ivResRef);
            op1 = itemView.findViewById(R.id.resRb1);
            op2 = itemView.findViewById(R.id.resRb2);
            op3 = itemView.findViewById(R.id.resRb3);
            op4 = itemView.findViewById(R.id.resRb4);
            tvPoint = itemView.findViewById(R.id.tvPoint);
        }
    }

    private void setScore() {
        tvScore.setText("Score :"+score+"/"+total);
        per =(int) (score*100)/total;
        tvPercentage.setText(String.valueOf(per)+"%");
        pbProgress.setMax(100);
        pbProgress.setProgress(per);
    }

    private void initViews() {
        tvScore = findViewById(R.id.tvScore);
        tvPercentage = findViewById(R.id.tvPercentage);
        pbProgress = findViewById(R.id.resProgress);
        rvRes = findViewById(R.id.rvRes);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
        rvRes.setLayoutManager(new LinearLayoutManager(this));
        score = getIntent().getIntExtra("score",-1);
        total = getIntent().getIntExtra("total",-1);
        quizId = getIntent().getStringExtra("id");
        uid = getIntent().getStringExtra("uid");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rank_menu,menu);
        return true;
    }

    //Options item selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        else if(item.getItemId() == R.id.lboard)
        {
            Intent intent = new Intent(ResultsActivity.this,LeaderboardActivity.class);
            intent.putExtra("id",quizId);
            startActivity(intent);
        }
        return true;
    }
}