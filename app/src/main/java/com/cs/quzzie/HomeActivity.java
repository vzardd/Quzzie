package com.cs.quzzie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cs.quzzie.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    CardView cvAttend,cvCreate,cvRateUs,cvShare,cvHistory,cvAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("");
        initViews();
        cvCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createIntent();
            }
        });
        cvAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageIntent();
            }
        });
        cvAttend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attendIntent();
            }
        });
        cvHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                historyIntent();
            }
        });
        cvRateUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rateUsIntent();
            }
        });
        cvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareIntent();
            }
        });
    }

    private void historyIntent() {
        Intent intent = new Intent(HomeActivity.this,HistoryActivity.class);
        startActivity(intent);
    }

    private void attendIntent() {
        Intent intent = new Intent(HomeActivity.this,AttendActivity.class);
        startActivity(intent);
    }

    private void manageIntent() {
        Intent intent = new Intent(HomeActivity.this,ManageActivity.class);
        startActivity(intent);
    }

    private void createIntent() {
        Intent intent = new Intent(HomeActivity.this,CreateQuizActivity.class);
        startActivity(intent);
    }

    private void shareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,"Hey! Try out this cool app that helps you to create and manage Quiz\n https://play.google.com/store/apps/details?id=com.cs.quizzie");
        startActivity(intent);
    }

    private void rateUsIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.cs.quizzie"));
        startActivity(intent);
    }

    private void initViews() {
        mAuth = FirebaseAuth.getInstance();
        cvAttend = findViewById(R.id.cvAttend);
        cvCreate = findViewById(R.id.cvCreate);
        cvAdmin = findViewById(R.id.cvAdmin);
        cvRateUs = findViewById(R.id.cvRate);
        cvHistory = findViewById(R.id.cvHistory);
        cvShare = findViewById(R.id.cvShare);
    }

    //On create options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu,menu);
        return true;
    }

    //On options item selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.mi_profile)
        {
            Intent intent = new Intent(HomeActivity.this,ProfileActivity.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.mi_logout)
        {
            mAuth.signOut();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        return true;
    }
}