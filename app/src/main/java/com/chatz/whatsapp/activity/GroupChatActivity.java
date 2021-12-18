package com.chatz.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;

import com.chatz.whatsapp.R;
import com.chatz.whatsapp.adapter.MessageAdapter;
import com.chatz.whatsapp.model.Group;
import com.chatz.whatsapp.model.Message;
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

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupRef, GroupMessageKeyRef;
    private Group currentGroup;
    private MessageAdapter messageAdapter;
    private RecyclerView recyclerView;
    private List<Message> messageList = new ArrayList<>();
    public static final String MESSAGE_KEY = "messages";

    private String currentGroupId, currentUserID, currentUserName, currentUserUid, currentDate, currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        currentGroupId = getIntent().getStringExtra("groupId");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        GroupMessageKeyRef = GroupRef.child(currentGroupId).child(MESSAGE_KEY);

        InitializeFields();

        GetUserInfo();
        GetGroupInfo();
        RetrieveAndDisplayGroups();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMessageInfoToDatabase();
                userMessageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void RetrieveAndDisplayGroups() {
        GroupMessageKeyRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Message> set = new ArrayList<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    Message message = ((DataSnapshot) iterator.next()).getValue(Message.class);
                    if(message!=null){
                        set.add(message);
                    }
                }
                messageList.clear();
                messageList.addAll(set);
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void GetGroupInfo() {
        GroupRef.child(currentGroupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentGroup = dataSnapshot.getValue(Group.class);
                    if (currentGroup!=null){
                        getSupportActionBar().setTitle(currentGroup.getName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void SaveMessageInfoToDatabase() {
        String messageTxt = userMessageInput.getText().toString();
        userMessageInput.setFocusable(true);
        if (TextUtils.isEmpty(messageTxt)) {
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show();
            return;
        }
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        currentDate = currentDateFormat.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calForTime.getTime());

        String id = GroupMessageKeyRef.push().getKey();
        Message message = new Message(id, currentUserUid, messageTxt, "text", null, currentTime, currentDate, currentUserName);
        GroupMessageKeyRef.child(id).setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                userMessageInput.requestFocus();
                if (!task.isSuccessful()) {
                    Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void GetUserInfo() {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                    currentUserUid = dataSnapshot.child("uid").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields() {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        userMessageInput = (EditText) findViewById(R.id.input_group_message);
        mScrollView = (ScrollView) findViewById(R.id.my_scroll_view);

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
