package com.cs.quzzie;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.ArrayMap;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddQuestionActivity extends AppCompatActivity {
    private static final int PICK_REF_IMAGE = 100;
    EditText etQuestion,etOp1,etOp2,etOp3,etOp4;
    RadioButton rb1,rb2,rb3,rb4;
    ImageView ivQnMic,ivMic1,ivMic2,ivMic3,ivMic4,ivAddImage,ivDeleteImage;
    Button btnSave;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    StorageReference storageRef;
    String qid,quizId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);
        getSupportActionBar().setTitle("Add Question");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initViews();
        if(!getIntent().getStringExtra("qid").trim().isEmpty())
        {
            qid = getIntent().getStringExtra("qid");
            initialize(getIntent().getStringExtra("qid"));
        }
        else {
            qid = rootRef.child("Questions").child(quizId).push().getKey();
        }
        ivAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,PICK_REF_IMAGE);
            }
        });
        ivDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteImage();
            }
        });
        //Voice input for Question
        ivQnMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Need to speak");
                try {
                    startActivityForResult(intent, 2);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(), "Sorry your device not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Voice input for Option 1
        ivMic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Need to speak");
                try {
                    startActivityForResult(intent, 3);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(), "Sorry your device not supported", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //Voice input for Option 2
        ivMic2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Need to speak");
                try {
                    startActivityForResult(intent, 4);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(), "Sorry your device not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Voice input for Option 3
        ivMic3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Need to speak");
                try {
                    startActivityForResult(intent, 5);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(), "Sorry your device not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Voice input for Option 4
        ivMic4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Need to speak");
                try {
                    startActivityForResult(intent, 6);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(), "Sorry your device not supported", Toast.LENGTH_SHORT).show();
                }

            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveQuestion();
            }
        });
    }

    private void deleteImage() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait...");
        dialog.show();
        storageRef.child("Questions").child(quizId).child(qid+".jpg").delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog.dismiss();
                        ivAddImage.setImageResource(R.drawable.addimagevector);
                        ivDeleteImage.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(AddQuestionActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == PICK_REF_IMAGE && data!=null)
        {
            addImage(data);
        }
        if(resultCode == RESULT_OK && data!=null)
        {
            ArrayList<String> result = new ArrayList<String>();
            result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(requestCode==2) {
                etQuestion.setText(etQuestion.getText().toString()+" "+result.get(0));
            }
            else if(requestCode==3)
            {
                etOp1.setText(result.get(0));
            }
            else if(requestCode==4)
            {
                etOp2.setText(result.get(0));
            }
            else if(requestCode==5)
            {
                etOp3.setText(result.get(0));
            }
            else if(requestCode==6)
            {
                etOp4.setText(result.get(0));
            }
        }
    }

    private void addImage(Intent data) {
        if(data.getData()!=null)
        {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setCancelable(false);
            dialog.setMessage("Please wait...");
            dialog.show();
            storageRef.child("Questions").child(quizId).child(qid+".jpg").putFile(data.getData())
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            Glide.with(AddQuestionActivity.this).load(data.getData()).into(ivAddImage);
                            ivDeleteImage.setVisibility(View.VISIBLE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(AddQuestionActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    //Writing Question to firebase
    public void saveQuestion()
    {
        if(checkIfFilled()) // if all fields are filled
        {
            String question = etQuestion.getText().toString().trim();
            String op1 = etOp1.getText().toString().trim();
            String op2 = etOp2.getText().toString().trim();
            String op3 = etOp3.getText().toString().trim();
            String op4 = etOp4.getText().toString().trim();
            int ans;
            if(rb1.isChecked())
            {
                ans = 1;
            }
            else if(rb2.isChecked())
            {
                ans = 2;
            }
            else if(rb3.isChecked())
            {
                ans = 3;
            }
            else
            {
                ans = 4;
            }
            saveToFirebase(question,op1,op2,op3,op4,ans);
        }
    }

    private void saveToFirebase(String question, String op1, String op2, String op3, String op4, int ans) {
        ProgressDialog dialog = new ProgressDialog(AddQuestionActivity.this);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait...");
        dialog.show();
        HashMap<String,Object> qn = new HashMap<String, Object>();
        qn.put("qid",qid);
        qn.put("question",question);
        qn.put("op1",op1);
        qn.put("op2",op2);
        qn.put("op3",op3);
        qn.put("op4",op4);
        qn.put("ans",ans);
        rootRef.child("Questions").child(quizId).child(qid).setValue(qn)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog.dismiss();
                        Toast.makeText(AddQuestionActivity.this, "Question added.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(AddQuestionActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    //Checking if all the fields are filled
    public boolean checkIfFilled()
    {
        if(etQuestion.getText().toString().trim().isEmpty())
        {
            etQuestion.setError("It cannot be empty!");
            return false;
        }
        else if(etOp1.getText().toString().trim().isEmpty())
        {
            etOp1.setError("It cannot be empty!");
            return false;
        }
        else if(etOp2.getText().toString().trim().isEmpty())
        {
            etOp2.setError("It cannot be empty!");
            return false;
        }
        else if(etOp3.getText().toString().trim().isEmpty() && !etOp4.getText().toString().trim().isEmpty())
        {
            etOp3.setError("Fill this option first!");
            return false;
        }
        else if(!rb1.isChecked() && !rb2.isChecked() && !rb3.isChecked() && !rb4.isChecked() )
        {
            Toast.makeText(this, "Choose correct answer!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(rb3.isChecked() && etOp3.getText().toString().trim().isEmpty())
        {
            Toast.makeText(this, "You can't choose empty answer!", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(rb4.isChecked() && etOp4.getText().toString().trim().isEmpty())
        {
            Toast.makeText(this, "You can't choose empty answer!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //Initializing values
    private void initialize(String qid) {
        ProgressDialog dialog = new ProgressDialog(AddQuestionActivity.this);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait...");
        dialog.show();
        rootRef.child("Questions").child(quizId).child(qid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dialog.dismiss();
                if(snapshot.exists())
                {
                        String question = (String) snapshot.child("question").getValue();
                        etQuestion.setText(question);
                        String op1 = (String) snapshot.child("op1").getValue();
                        etOp1.setText(op1);
                        String op2 = (String) snapshot.child("op2").getValue();
                        etOp2.setText(op2);
                        String op3 = (String) snapshot.child("op3").getValue();
                        etOp3.setText(op3);
                        String op4 = (String) snapshot.child("op4").getValue();
                        etOp4.setText(op4);
                        long ans = (long) snapshot.child("ans").getValue();
                        switch ((int) ans) {
                            case 1:
                                rb1.setChecked(true);
                                break;
                            case 2:
                                rb2.setChecked(true);
                                break;
                            case 3:
                                rb3.setChecked(true);
                                break;
                            case 4:
                                rb4.setChecked(true);
                                break;
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
            }
        });
        storageRef.child("Questions").child(quizId).child(qid+".jpg").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if(uri!=null)
                        {
                            Glide.with(AddQuestionActivity.this).load(uri.toString()).into(ivAddImage);
                            ivDeleteImage.setVisibility(View.VISIBLE);
                        }
                        else {
                            ivAddImage.setImageResource(R.drawable.addimagevector);
                            ivDeleteImage.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ivAddImage.setImageResource(R.drawable.addimagevector);
                        ivDeleteImage.setVisibility(View.GONE);
                    }
                });
    }

    //Initializing views
    private void initViews() {
        etQuestion = findViewById(R.id.etQuestion);
        ivAddImage = findViewById(R.id.ivAddImage);
        ivDeleteImage = findViewById(R.id.ivDeleteRefImage);
        ivDeleteImage.setVisibility(View.GONE);
        quizId = getIntent().getStringExtra("id");
        etOp1 = findViewById(R.id.etOption1);
        etOp2 = findViewById(R.id.etOption2);
        etOp3 = findViewById(R.id.etOption3);
        etOp4 = findViewById(R.id.etOption4);
        rb1 = findViewById(R.id.radioButton1);
        rb2 = findViewById(R.id.radioButton2);
        rb3 = findViewById(R.id.radioButton3);
        rb4 = findViewById(R.id.radioButton4);
        ivQnMic = findViewById(R.id.ivQnMic);
        ivMic1 = findViewById(R.id.ivMic1);
        ivMic2 = findViewById(R.id.ivMic2);
        ivMic3 = findViewById(R.id.ivMic3);
        ivMic4 = findViewById(R.id.ivMic4);
        btnSave = findViewById(R.id.btnSave);
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

    @Override
    public void onBackPressed() {
        if(getIntent().getStringExtra("qid").isEmpty())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("Are you sure, you want to go back?");
            builder.setMessage("This question will not be saved!");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.setPositiveButton("Go Back!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    ProgressDialog dialog = new ProgressDialog(AddQuestionActivity.this);
                    dialog.setCancelable(false);
                    dialog.setMessage("Please wait...");
                    dialog.show();
                    rootRef.child("Questions").child(quizId).child(qid).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    Toast.makeText(AddQuestionActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }
            });
            builder.show();
        }
        else {
            super.onBackPressed();
        }
    }
}