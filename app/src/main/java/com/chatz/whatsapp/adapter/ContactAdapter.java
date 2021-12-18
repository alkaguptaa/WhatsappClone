package com.chatz.whatsapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chatz.whatsapp.R;
import com.chatz.whatsapp.activity.ProfileActivity;
import com.chatz.whatsapp.model.Contact;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private List<Contact> contactList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private Context context;


    public ContactAdapter(Context context, List<Contact> contactList) {
        this.contactList = contactList;
        this.context = context;
    }

    
    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
        ContactViewHolder viewHolder = new ContactViewHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull final ContactViewHolder holder, final int position) {
        final Contact model = contactList.get(position);
        holder.itemView.setVisibility(View.VISIBLE);
        holder.userName.setText(model.getName());
        holder.userStatus.setText(model.getStatus());
        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(context, ProfileActivity.class);
                profileIntent.putExtra("visit_user_id", model.getUid());
                context.startActivity(profileIntent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return contactList.size();
    }



    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profileImage;
        View wrapperLayout;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            wrapperLayout = itemView.findViewById(R.id.wrapperLayout);
        }
    }
}
