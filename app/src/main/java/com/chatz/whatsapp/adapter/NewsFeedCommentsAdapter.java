package com.chatz.whatsapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chatz.whatsapp.R;
import com.chatz.whatsapp.model.Comments;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewsFeedCommentsAdapter extends RecyclerView.Adapter<NewsFeedCommentsAdapter.MessageViewHolder> {
    private List<Comments> userMessageList;
    private FirebaseAuth mAuth;
    private Context context;
    private DatabaseReference feedCommentRef = FirebaseDatabase.getInstance().getReference().child("Comments");

    public NewsFeedCommentsAdapter(Context context, List<Comments> userMessageList) {
        this.context = context;
        this.userMessageList = userMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_comment_view, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        final String currentUserId = mAuth.getCurrentUser().getUid();
        final Comments comments = userMessageList.get(position);

        Picasso.get().load(comments.getSenderAvatar()).placeholder(R.drawable.profile_image).into(holder.imgUserAvatar);

        holder.tvComment.setText(comments.getComment());
        holder.tvUserName.setText(comments.getSenderName());
        holder.tvTime.setText(comments.getTime());

    }


    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView imgUserAvatar;
        public TextView tvUserName;
        public TextView tvComment;
        public TextView tvTime;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            tvComment = itemView.findViewById(R.id.tvComment);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
        }
    }


}