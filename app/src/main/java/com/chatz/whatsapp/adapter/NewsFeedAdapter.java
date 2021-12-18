package com.chatz.whatsapp.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chatz.whatsapp.R;
import com.chatz.whatsapp.Utils.Const;
import com.chatz.whatsapp.activity.FeedCommentsActivity;
import com.chatz.whatsapp.activity.FeedSendToActivity;
import com.chatz.whatsapp.model.NewsFeed;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewsFeedAdapter extends RecyclerView.Adapter<NewsFeedAdapter.MessageViewHolder> {
    private List<NewsFeed> userMessageList;
    private FirebaseAuth mAuth;
    private Context context;
    private DatabaseReference feedLikeRef = FirebaseDatabase.getInstance().getReference().child("NewsFeed");

    public NewsFeedAdapter(Context context, List<NewsFeed> userMessageList) {
        this.context = context;
        this.userMessageList = userMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_newsfeed, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        final String currentUserId = mAuth.getCurrentUser().getUid();
        final NewsFeed feed = userMessageList.get(position);

        Picasso.get().load(feed.getImage()).placeholder(R.drawable.profile_image).into(holder.imgFeedImage);
        Picasso.get().load(feed.getSenderAvatar()).placeholder(R.drawable.profile_image).into(holder.imgSenderAvatar);

        holder.tvMessage.setText(feed.getMessage());

        holder.tvSenderName.setText(feed.getSenderName());

        holder.tvTime.setText(feed.getTime());

        holder.tvLikeCount.setText(feed.getListOfLikes().size() + "");

        holder.tvCommentCount.setText(feed.getCommentsCount() + "");

        final boolean isSelfLike = feed.getListOfLikes().contains(currentUserId);
        holder.imgLike.setImageResource(isSelfLike ? R.drawable.ic_fv : R.drawable.ic_non_fv);

        holder.llLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                funLikeFeed(feed, currentUserId, isSelfLike);
            }
        });

        holder.llComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FeedCommentsActivity.class);
                intent.putExtra("feedID", feed.getId());
                context.startActivity(intent);
            }
        });

        holder.llSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FeedSendToActivity.class);
                intent.putExtra("feedID", feed.getId());
                context.startActivity(intent);
            }
        });

        holder.imgOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPopupMenu(v, feed);
            }
        });

    }

    private void openPopupMenu(View view, final NewsFeed feed) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenu().add("Share feed");

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getTitle().toString()) {
                    case "Share feed":
                        shareFeed(feed.getMessage(), feed.getImage());
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        popup.show();
    }

    private void shareFeed(final String msg, String url) {

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        Picasso.get().load(url).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                progressDialog.dismiss();
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, msg);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Const.getLocalBitmapUri(context, bitmap));
                shareIntent.setType("image/*");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(Intent.createChooser(shareIntent, "send"));
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                progressDialog.dismiss();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });
    }

    private void funLikeFeed(NewsFeed feed, String currentUserId, final boolean isSelfLike) {
        List<String> gg = feed.getListOfLikes();
        if (isSelfLike) {
            gg.remove(currentUserId);
        } else {
            gg.add(currentUserId);
        }
        feedLikeRef.child(feed.getId()).child("listOfLikes").setValue(gg).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // like and remove
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView tvSenderName;
        public TextView tvTime;
        public TextView tvMessage;
        public CircleImageView imgSenderAvatar;
        public ImageView imgFeedImage;
        public ImageView imgOption;
        public ImageView imgLike;
        public LinearLayout llLike;
        public LinearLayout llComments;
        public LinearLayout llSend;
        public TextView tvLikeCount;
        public TextView tvCommentCount;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSenderName = itemView.findViewById(R.id.feed_posttitle);
            tvTime = itemView.findViewById(R.id.feed_timeStamp);
            tvMessage = itemView.findViewById(R.id.feed_usertxt);
            imgSenderAvatar = itemView.findViewById(R.id.profile_picture);
            imgFeedImage = itemView.findViewById(R.id.feed_userimg);
            imgLike = itemView.findViewById(R.id.like_btn);
            llLike = itemView.findViewById(R.id.llLike);
            llComments = itemView.findViewById(R.id.llComments);
            llSend = itemView.findViewById(R.id.llSend);
            tvLikeCount = itemView.findViewById(R.id.like_number);
            imgOption = itemView.findViewById(R.id.feed_options);
            tvCommentCount = itemView.findViewById(R.id.comment_number);
        }
    }


}