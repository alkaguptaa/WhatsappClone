package com.chatz.whatsapp.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chatz.whatsapp.R;
import com.chatz.whatsapp.adapter.NewsFeedCommentsAdapter;
import com.chatz.whatsapp.model.Comments;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class FeedCommentsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvList;
    private ProgressBar pbBar;
    private EditText etComment;
    private TextView tvNoData;
    private ImageButton ibSend;
    private List<Comments> commentsList = new ArrayList<>();
    private NewsFeedCommentsAdapter adapter;

    private String feedID = "";
    private FirebaseAuth mAuth;

    private int commentsCount = 0;
    private String currentUserName;
    private String currentUserID;
    private String currentUserAvatar;
    private DatabaseReference usersRef;
    private DatabaseReference cmntRef;
    private DatabaseReference commentedDataRef;
    private DatabaseReference feedCommentRef;
    private DatabaseReference newsFeedRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_comments_activity);

        feedID = getIntent().getStringExtra("feedID");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        cmntRef = FirebaseDatabase.getInstance().getReference();
        commentedDataRef = FirebaseDatabase.getInstance().getReference().child("Comments");
        feedCommentRef = FirebaseDatabase.getInstance().getReference().child("NewsFeed");
        newsFeedRef = FirebaseDatabase.getInstance().getReference().child("NewsFeed").child(feedID);

        setToolbar();

        initView();

        getUserInfo();

        getNewsfFeed();

        getCommentesData();

        ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etComment.getText().toString().trim().isEmpty()) {
                    Toast.makeText(FeedCommentsActivity.this, "Please enter comment", Toast.LENGTH_SHORT).show();
                } else {

                    Calendar calendar = Calendar.getInstance();

                    SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
                    String saveCurrentDate = currentDate.format(calendar.getTime());

                    SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                    String saveCurrentTime = currentTime.format(calendar.getTime());

                    Comments comments = new Comments();
                    comments.setDate(saveCurrentDate);
                    comments.setTime(saveCurrentTime);
                    comments.setComment(etComment.getText().toString().trim());
                    comments.setSenderName(currentUserName);
                    comments.setSenderAvatar(currentUserAvatar);

                    addFeedComment(comments);
                    etComment.setText("");
                }
            }
        });

    }

    private void getNewsfFeed() {
        newsFeedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String count = dataSnapshot.child("commentsCount").getValue() + "";
                    if (count != null && !count.equalsIgnoreCase("null")) {
                        commentsCount = Integer.parseInt(count);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getCommentesData() {
        commentedDataRef.child(feedID).orderByValue().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Comments> set = new ArrayList<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    Comments cmnts = ((DataSnapshot) iterator.next()).getValue(Comments.class);
                    if (cmnts != null) {
                        set.add(cmnts);
                    }
                }
                commentsList.clear();
                commentsList.addAll(set);
                tvNoData.setVisibility(commentsList.isEmpty() ? View.VISIBLE : View.GONE);
                pbBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                if (adapter.getItemCount() != 0) {
                    rvList.smoothScrollToPosition(adapter.getItemCount() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addFeedComment(Comments comments) {
        String commentID = cmntRef.child("Comments").push().getKey();
        comments.setId(commentID);
        cmntRef.child("Comments").child(feedID).child(commentID).setValue(comments).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    funUpdateCommentCount();
                    Toast.makeText(FeedCommentsActivity.this, "Commented", Toast.LENGTH_SHORT).show();
                } else {
                    // error
                }
            }
        });
    }

    private void funUpdateCommentCount() {
        int count = commentsCount + 1;
        feedCommentRef.child(feedID).child("commentsCount").setValue(count).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // count updated
                }
            }
        });
    }

    private void getUserInfo() {
        usersRef = FirebaseDatabase.getInstance().getReference();
        usersRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue() + "";
                    currentUserAvatar = dataSnapshot.child("image").getValue() + "";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initView() {
        etComment = findViewById(R.id.etComment);
        ibSend = findViewById(R.id.ibSend);
        tvNoData = findViewById(R.id.tvNoData);
        pbBar = findViewById(R.id.pbBar);
        rvList = findViewById(R.id.rvList);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setReverseLayout(true);
        lm.setStackFromEnd(true);

        rvList.setLayoutManager(lm);

        adapter = new NewsFeedCommentsAdapter(this, commentsList);
        rvList.setAdapter(adapter);

    }


    private void setToolbar() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}