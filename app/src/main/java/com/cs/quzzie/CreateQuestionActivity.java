package com.cs.quzzie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cs.quzzie.model.QuestionClass;
import com.cs.quzzie.model.QuizClass;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CreateQuestionActivity extends AppCompatActivity {
    TextView tvQuizId,tvQnEmpty;
    RecyclerView rvQns;
    ImageView ivCopy;
    FloatingActionButton fabAdd,fabShare;
    String quizName,quizId;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_question);
        getSupportActionBar().setTitle(getIntent().getStringExtra("name"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initViews();
        tvQuizId.setText(quizId);
        ivCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clip.setPrimaryClip(ClipData.newPlainText("id",quizId));
                Toast.makeText(CreateQuestionActivity.this, "Quiz ID copied!", Toast.LENGTH_SHORT).show();
            }
        });
        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareQuiz();
            }
        });
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateQuestionActivity.this,AddQuestionActivity.class);
                intent.putExtra("qid","");
                intent.putExtra("id",quizId);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<QuestionClass> options = new FirebaseRecyclerOptions.Builder<QuestionClass>()
                .setQuery(rootRef.child("Questions").child(quizId),QuestionClass.class)
                .build();
        FirebaseRecyclerAdapter<QuestionClass,QnViewHolder> adapter = new FirebaseRecyclerAdapter<QuestionClass, QnViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull QnViewHolder holder, int position, @NonNull QuestionClass model) {
                setQn(holder,model);
                holder.clQn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(CreateQuestionActivity.this,AddQuestionActivity.class);
                        intent.putExtra("qid",model.getQid());
                        intent.putExtra("id",quizId);
                        startActivity(intent);
                    }
                });
                holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(CreateQuestionActivity.this);
                        builder.setTitle("Are you sure?");
                        builder.setMessage("This question will be deleted permanently!");
                        builder.setCancelable(false);
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                deleteQn(model.getQid());
                            }
                        });
                        builder.show();
                    }
                });
            }

            @NonNull
            @Override
            public QnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.qn_row_layout,parent,false);
                return new QnViewHolder(v);
            }
        };
        rvQns.setAdapter(adapter);
        adapter.startListening();
        rootRef.child("Questions").child(quizId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    tvQnEmpty.setVisibility(View.GONE);
                }
                else {
                    tvQnEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateQuestionActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //deleting question
    private void deleteQn(String qid) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait...");
        dialog.show();
        rootRef.child("Questions").child(quizId).child(qid).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog.dismiss();
                        Toast.makeText(CreateQuestionActivity.this, "Question Deleted.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(CreateQuestionActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setQn(QnViewHolder holder, QuestionClass model) {
        if(model.getQuestion().length()<15) {
            holder.tvQnTitle.setText(model.getQuestion());
        }
        else {
            holder.tvQnTitle.setText(model.getQuestion().substring(0,14)+"...");
        }
    }

    public static class QnViewHolder extends RecyclerView.ViewHolder{
        ConstraintLayout clQn;
        TextView tvQnTitle;
        ImageView ivDelete;

        public QnViewHolder(@NonNull View itemView) {
            super(itemView);
            clQn = itemView.findViewById(R.id.clQn);
            tvQnTitle = itemView.findViewById(R.id.tvQnName);
            ivDelete = itemView.findViewById(R.id.ivDeleteQn);
        }
    }

    private void shareQuiz() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,"You're invited to attend the Quiz,\n\n"+quizName+"\n\nQuiz ID: "+quizId+"\n\nIf you don't have Quizzie installed, get it now on playstore!\n\nhttps://play.google.com/store/apps/details?id=com.cs.quizzie");
        startActivity(intent);
    }

    private void initViews() {
        tvQuizId = findViewById(R.id.tvQuizKey);
        tvQnEmpty = findViewById(R.id.tvQnEmpty);
        tvQnEmpty.setVisibility(View.GONE);
        ivCopy = findViewById(R.id.ivCopy);
        rvQns = findViewById(R.id.rvQuestionsList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvQns.setLayoutManager(layoutManager);
        rvQns.setHasFixedSize(true);
        fabAdd = findViewById(R.id.fabAddQn);
        fabShare = findViewById(R.id.fabShareQuiz);
        quizName = getIntent().getStringExtra("name");
        quizId = getIntent().getStringExtra("key");
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu,menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.mi_accept).setChecked(getIntent().getBooleanExtra("accepting",false));
        return true;
    }

    //Options item selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        else if(item.getItemId()==R.id.mi_leaderboard)
        {
            Intent intent = new Intent(CreateQuestionActivity.this,LeaderboardActivity.class);
            intent.putExtra("id",quizId);
            startActivity(intent);
        }
        else if(item.getItemId()==R.id.mi_accept)
        {
            if(item.isChecked())
            {
                unCheck(item);
            }
            else {
                check(item);
            }
        }
        return true;
    }

    private void check(MenuItem item) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait...");
        dialog.show();
        rootRef.child("Quiz").child(quizId).child("accepting").setValue(true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog.dismiss();
                        Toast.makeText(CreateQuestionActivity.this, "Started accepting responses", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(CreateQuestionActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void unCheck(MenuItem item) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait...");
        dialog.show();
        rootRef.child("Quiz").child(quizId).child("accepting").setValue(false)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog.dismiss();
                        Toast.makeText(CreateQuestionActivity.this, "Stopped accepting responses", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(CreateQuestionActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}