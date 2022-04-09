package com.cs.quzzie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cs.quzzie.model.QuestionClass;
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

import java.util.HashMap;

public class QuizActivity extends AppCompatActivity {
    RecyclerView rvQuiz;
    Button btnSubmit;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    StorageReference storageRef;
    String quizId;
    int [] ansrs;
    boolean [] sol;
    String [] qids;
    int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        getSupportActionBar().setTitle(getIntent().getStringExtra("name"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        initViews();
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate())
                {
                    submit();
                }
            }
        });
    }

    //Submitting answers
    private void submit() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait...");
        dialog.show();
        score=0;
        for(boolean bool : sol)
        {
            if(bool)
            {
                score++;
            }
        }
        HashMap<String,Object> result = new HashMap<String, Object>();
        result.put("id",quizId);
        result.put("uid",mAuth.getCurrentUser().getUid());
        result.put("score",score);
        result.put("total",sol.length);
        rootRef.child("Ranking").child(quizId).child(mAuth.getCurrentUser().getUid()).setValue(result)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        rootRef.child("Attempts").child(mAuth.getCurrentUser().getUid()).child(quizId).setValue(result)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        HashMap<String,Object> res;
                                        for(int i=0;i<sol.length;++i)
                                        {
                                            res = new HashMap<String, Object>();
                                            res.put("qid",qids[i]);
                                            res.put("marked",ansrs[i]);
                                            res.put("result",sol[i]);
                                            rootRef.child("Results").child(quizId).child(mAuth.getCurrentUser().getUid()).child(qids[i]).setValue(res);
                                        }
                                        dialog.dismiss();
                                        Intent intent = new Intent(QuizActivity.this,ResultsActivity.class);
                                        intent.putExtra("score",score);
                                        intent.putExtra("total",sol.length);
                                        intent.putExtra("id",quizId);
                                        intent.putExtra("uid",mAuth.getCurrentUser().getUid());
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.dismiss();
                                        Toast.makeText(QuizActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(QuizActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    //Validating ansrs
    private boolean validate() {
        for(int ans : ansrs)
        {
            if(ans==0)
            {
                Toast.makeText(this, "One are more questions are not answered!", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<QuestionClass> options = new FirebaseRecyclerOptions.Builder<QuestionClass>()
                .setQuery(rootRef.child("Questions").child(quizId),QuestionClass.class)
                .build();
        FirebaseRecyclerAdapter<QuestionClass,AttendQnVH> adapter = new FirebaseRecyclerAdapter<QuestionClass, AttendQnVH>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AttendQnVH holder, int position, @NonNull QuestionClass model) {
                setView(holder,position,model);
                qids[position] = model.getQid();
                switch (ansrs[position])
                {
                    case 1: holder.op1.setChecked(true);
                    break;
                    case 2: holder.op2.setChecked(true);
                    break;
                    case 3: holder.op3.setChecked(true);
                    break;
                    case 4:holder.op4.setChecked(true);
                    break;
                }
                holder.op1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if(b)
                        {
                            ansrs[position] = 1;
                            sol[position] = model.getAns() == 1;
                        }
                    }
                });
                holder.op2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if(b)
                        {
                            ansrs[position] = 2;
                            sol[position] = model.getAns() == 2;
                        }
                    }
                });
                holder.op3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if(b)
                        {
                            ansrs[position] = 3;
                            sol[position] = model.getAns() == 3;
                        }
                    }
                });
                holder.op4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if(b)
                        {
                            ansrs[position] = 4;
                            sol[position] = model.getAns() == 4;
                        }
                    }
                });
            }

            @NonNull
            @Override
            public AttendQnVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.attend_quiz_row_layout,parent,false);
                return new AttendQnVH(v);
            }
        };
        rvQuiz.setAdapter(adapter);
        adapter.startListening();
    }

    private void setView(AttendQnVH holder, int position, QuestionClass model) {
        holder.tvQnNo.setText("Question "+String.valueOf(position+1)+" :");
        holder.tvQn.setText(model.getQuestion());
        storageRef.child("Questions").child(quizId).child(model.getQid()+".jpg").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if(uri!=null)
                        {
                            holder.ivRef.setVisibility(View.VISIBLE);
                            Glide.with(QuizActivity.this).load(uri.toString()).into(holder.ivRef);
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
        holder.op1.setText(model.getOp1());
        holder.op2.setText(model.getOp2());
        if(model.getOp3().trim().isEmpty())
        {
            holder.op3.setVisibility(View.GONE);
        }
        else {
            holder.op3.setVisibility(View.VISIBLE);
            holder.op3.setText(model.getOp3());
        }
        if(model.getOp4().trim().isEmpty())
        {
            holder.op4.setVisibility(View.GONE);
        }
        else {
            holder.op4.setVisibility(View.VISIBLE);
            holder.op4.setText(model.getOp4());
        }
    }

    public static class AttendQnVH extends RecyclerView.ViewHolder{
        TextView tvQnNo,tvQn;
        ImageView ivRef;
        RadioButton op1,op2,op3,op4;

        public AttendQnVH(@NonNull View itemView) {
            super(itemView);
            tvQnNo = itemView.findViewById(R.id.tvQnNo);
            tvQn = itemView.findViewById(R.id.tvDisplayQn);
            ivRef = itemView.findViewById(R.id.ivRef);
            op1 = itemView.findViewById(R.id.rb1);
            op2 = itemView.findViewById(R.id.rb2);
            op3 = itemView.findViewById(R.id.rb3);
            op4 = itemView.findViewById(R.id.rb4);
        }
    }

    private void initViews() {
        rvQuiz = findViewById(R.id.rvQuizQns);
        rvQuiz.setLayoutManager(new LinearLayoutManager(this));
        btnSubmit = findViewById(R.id.btnSubmitQuiz);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
        quizId = getIntent().getStringExtra("id");
        rootRef.child("Questions").child(quizId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int n = (int) snapshot.getChildrenCount();
                ansrs = new int[n];
                sol = new boolean[n];
                qids = new String[n];
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuizActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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