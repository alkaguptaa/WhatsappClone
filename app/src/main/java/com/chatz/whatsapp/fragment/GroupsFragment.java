package com.chatz.whatsapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.chatz.whatsapp.R;
import com.chatz.whatsapp.activity.GroupChatActivity;
import com.chatz.whatsapp.model.Group;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class GroupsFragment extends Fragment {
    private View groupFragmentView;
    private ListView list_view;
    private ArrayAdapter<Group> arrayAdapter;
    public Button button1;
    public Button button2;
    private ArrayList<Group> list_of_groups = new ArrayList<>();
    private DatabaseReference GroupRef;

    public GroupsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        button1 = groupFragmentView.findViewById(R.id.button1);
        button2 = groupFragmentView.findViewById(R.id.button2);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button1.setBackgroundResource(R.color.colorPrimary);
                button2.setBackgroundResource(R.color.grey);
                InitializePublicFields();

                RetrieveAndDisplayPublicGroups();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button2.setBackgroundResource(R.color.colorPrimary);
                button1.setBackgroundResource(R.color.grey);
                InitializePrivateFields();


                RetrieveAndDisplayPrivateGroups();
            }
        });



        InitializePublicFields();

        RetrieveAndDisplayPublicGroups();

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Group group = (Group)adapterView.getItemAtPosition(position);
                Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
                groupChatIntent.putExtra("groupId", group.getId());
                startActivity(groupChatIntent);
            }
        });

        return groupFragmentView;
    }


    private void RetrieveAndDisplayPublicGroups() {
        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<Group> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    Group group = ((DataSnapshot) iterator.next()).getValue(Group.class);
                    if(group!=null){
                        if(group.getId().contains("-pub"))
                        {
                            set.add(group);
                        }

                    }
                }

                list_of_groups.clear();
                list_of_groups.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitializePublicFields() {
        list_view = (ListView) groupFragmentView.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<Group>(getContext(), android.R.layout.simple_list_item_1, list_of_groups){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if(convertView==null){
                    convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
                }
                ((TextView) convertView.findViewById(android.R.id.text1)).setText(getItem(position).getName());
                return convertView;
            }
        };
        list_view.setAdapter(arrayAdapter);
    }


    private void RetrieveAndDisplayPrivateGroups() {
        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<Group> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    Group group = ((DataSnapshot) iterator.next()).getValue(Group.class);
                    if(group!=null){
                        if(group.getId().contains("-pri"))
                        {
                            set.add(group);
                        }

                    }
                }

                list_of_groups.clear();
                list_of_groups.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitializePrivateFields() {
        list_view = (ListView) groupFragmentView.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<Group>(getContext(), android.R.layout.simple_list_item_1, list_of_groups){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if(convertView==null){
                    convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
                }
                ((TextView) convertView.findViewById(android.R.id.text1)).setText(getItem(position).getName());
                return convertView;
            }
        };
        list_view.setAdapter(arrayAdapter);
    }



}
