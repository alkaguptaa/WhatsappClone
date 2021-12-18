package com.chatz.whatsapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chatz.whatsapp.R;
import com.chatz.whatsapp.activity.CreateNewsFeedActivity;
import com.chatz.whatsapp.adapter.NewsFeedAdapter;
import com.chatz.whatsapp.model.NewsFeed;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class NewsFeedFragment extends Fragment {

    private RecyclerView rvList;
    private ProgressBar pbBar;
    private FloatingActionButton fbCreateFeed;
    private TextView tvNoData;
    private NewsFeedAdapter adapter;
    private DatabaseReference newsFeedDataRef;
    private DatabaseReference contactRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private List<String> userIdArray = new ArrayList<>();
    private List<NewsFeed> newsFeedList = new ArrayList<>();

    public NewsFeedFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_feed_fragment, container, false);

        intView(view);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        newsFeedDataRef = FirebaseDatabase.getInstance().getReference().child("NewsFeed");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);

        fbCreateFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreateNewsFeedActivity.class);
                startActivity(intent);
            }
        });

        getFriendsArray();

        return view;
    }

    private void intView(View view) {
        fbCreateFeed = view.findViewById(R.id.fbCreateFeed);
        tvNoData = view.findViewById(R.id.tvNoData);
        pbBar = view.findViewById(R.id.pbBar);
        rvList = view.findViewById(R.id.rvList);

        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        lm.setReverseLayout(true);
        lm.setStackFromEnd(true);

        rvList.setLayoutManager(lm);

        adapter = new NewsFeedAdapter(getActivity(), newsFeedList);
        rvList.setAdapter(adapter);
    }

    private void getFriendsArray() {
        contactRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userIdArray.clear();
                userIdArray.add(currentUserID);
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    DataSnapshot dss = (DataSnapshot) iterator.next();
                    String userId = dss.getKey();
                    HashMap<String, String> map = (HashMap<String, String>) dss.getValue();
                    if (map.containsKey("Contact") && map.get("Contact").equalsIgnoreCase("Saved")) {
                        userIdArray.add(userId);
                    }
                }
                getNewsFeedData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getNewsFeedData() {
        newsFeedDataRef.orderByChild("time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<NewsFeed> set = new ArrayList<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    NewsFeed message = ((DataSnapshot) iterator.next()).getValue(NewsFeed.class);
                    if (message != null && userIdArray.contains(message.getSenderID())) {
                        set.add(message);
                    }
                }
                newsFeedList.clear();
                newsFeedList.addAll(set);
                tvNoData.setVisibility(newsFeedList.isEmpty() ? View.VISIBLE : View.GONE);
                pbBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}