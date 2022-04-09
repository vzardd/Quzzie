package com.cs.quzzie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cs.quzzie.model.ManageClass;
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

public class ManageActivity extends AppCompatActivity {
    TextView tvQuizEmpty;
    RecyclerView rvQuizzes;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);
        getSupportActionBar().setTitle("Manage");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<ManageClass> options = new FirebaseRecyclerOptions.Builder<ManageClass>()
                .setQuery(rootRef.child("Manage").child(mAuth.getCurrentUser().getUid()),ManageClass.class)
                .build();
        FirebaseRecyclerAdapter<ManageClass,QuizViewHolder> adapter = new FirebaseRecyclerAdapter<ManageClass, QuizViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull QuizViewHolder holder, int position, @NonNull ManageClass model) {
                String id = model.getId();
                if(id.length()>0)
                {
                    setView(holder,id);
                }
                holder.clQuiz.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        viewQuiz(model.getId());
                    }
                });
                holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ManageActivity.this);
                        builder.setCancelable(false);
                        builder.setTitle("Are you sure?");
                        builder.setMessage("This quiz will be deleted!");
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
                                deleteQuiz(model.getId());
                            }
                        });
                        builder.show();
                    }
                });
            }

            @NonNull
            @Override
            public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.manage_row_layout,parent,false);
                return new QuizViewHolder(v);
            }
        };
        rvQuizzes.setAdapter(adapter);
        adapter.startListening();
        rootRef.child("Manage").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    tvQuizEmpty.setVisibility(View.GONE);
                }
                else {
                    tvQuizEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteQuiz(String id) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait...");
        dialog.show();
        rootRef.child("Manage").child(mAuth.getCurrentUser().getUid()).child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog.dismiss();
                        Toast.makeText(ManageActivity.this, "Quiz deleted successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(ManageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void viewQuiz(String id) {

        rootRef.child("Quiz").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Intent intent = new Intent(ManageActivity.this,CreateQuestionActivity.class);
                intent.putExtra("name", (String) snapshot.child("name").getValue());
                intent.putExtra("key",(String) snapshot.child("id").getValue());
                intent.putExtra("accepting",(Boolean) snapshot.child("accepting").getValue());
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setView(QuizViewHolder holder, String id) {
        rootRef.child("Quiz").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    holder.tvName.setText((CharSequence) snapshot.child("name").getValue());
                    if((Boolean) snapshot.child("accepting").getValue())
                    {
                        holder.tvAccept.setText("Accepting Response");
                    }
                    else {
                        holder.tvAccept.setText("Closed");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public static class QuizViewHolder extends RecyclerView.ViewHolder{
        TextView tvName,tvAccept;
        ImageView ivDelete;
        ConstraintLayout clQuiz;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvQuizRowTitle);
            tvAccept = itemView.findViewById(R.id.tvAccept);
            ivDelete = itemView.findViewById(R.id.ivDeleteQuiz);
            clQuiz = itemView.findViewById(R.id.clQuiz);
        }
    }

    private void initViews() {
        tvQuizEmpty = findViewById(R.id.tvQuizEmpty);
        tvQuizEmpty.setVisibility(View.GONE);
        rvQuizzes = findViewById(R.id.rvQuizzes);
        rvQuizzes.setLayoutManager(new LinearLayoutManager(this));
        rvQuizzes.setHasFixedSize(true);
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