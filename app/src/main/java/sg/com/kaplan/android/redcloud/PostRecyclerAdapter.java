package sg.com.kaplan.android.redcloud;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder> {

    public List<Post> postList;
    public List<User> userList;
    public Context context;
    public PostRecyclerAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public PostRecyclerAdapter(List<Post> postList, List<User> userList) {
        this.postList = postList;
        this.userList = userList;
        this.adapter = this;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list_item, parent, false);
        context = parent.getContext();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final String postId = postList.get(position).PostId;
        final String currentUserId = mAuth.getCurrentUser().getUid();

        final String descData = postList.get(position).getDesc();
        holder.setDescText(descData);

        final String image_url = postList.get(position).getImage_url();
        String thumb_url = postList.get(position).getThumb_url();
        holder.setPostImage(image_url, thumb_url);

        final String title = postList.get(position).getTitle();
        holder.setTitleText(title);

        String post_user_id = postList.get(position).getUser_id();

        if (post_user_id.equals(currentUserId)) {
            holder.postDeleteBtn.setEnabled(true);
            holder.postDeleteBtn.setVisibility(View.VISIBLE);
        } else {
            holder.postDeleteBtn.setEnabled(false);
            holder.postDeleteBtn.setVisibility(View.INVISIBLE);
        }

        final String userName = userList.get(position).getName();
        String userImage = userList.get(position).getImage();

        holder.setUserData(userName, userImage);

        try {
            long dateInMs = postList.get(position).getTimestamp().getTime();
            String postDate = DateFormat.format("dd MMM yyyy", new Date(dateInMs)).toString();
            holder.setDate(postDate);
        } catch (Exception e) {
            // Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Exception: ", e.getMessage());
        }

        // Get Likes Count
        db.collection("Posts/" + postId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshots != null) {
                    if (!documentSnapshots.isEmpty()) {
                        int count = documentSnapshots.size();
                        holder.updateLikesCount(count);
                    } else {
                        holder.updateLikesCount(0);
                    }
                }
            }
        });

        // Get Likes
        db.collection("Posts/" + postId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    if (documentSnapshot.exists()) {
                        holder.postLikeBtn.setImageResource(R.drawable.ic_favorite_active);
                    } else {
                        holder.postLikeBtn.setImageResource(R.drawable.ic_favorite_default);
                    }
                }
            }
        });


        // Like button press handler
        holder.postLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                db.collection("Posts/" + postId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()) {
                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            db.collection("Posts/" + postId + "/Likes").document(currentUserId).set(likesMap);
                        } else {
                            db.collection("Posts/" + postId + "/Likes").document(currentUserId).delete();
                        }
                    }
                });
            }
        });

        holder.postCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("post_id", postId);
                context.startActivity(commentIntent);
            }
        });

        holder.postDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Post")
                        .setMessage("You are about to delete this post, are you sure?")
                        .setIcon(R.drawable.ic_delete_accent)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.collection("Posts").document(postId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        postList.remove(position);
                                        userList.remove(position);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).setNegativeButton("Cancel", null).show();
            }
        });

        holder.postCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent postDetailIntent = new Intent(context, PostDetailsActivity.class);
                postDetailIntent.putExtra("Title", title);
                postDetailIntent.putExtra("Desc", descData);
                postDetailIntent.putExtra("Image", image_url);
                context.startActivity(postDetailIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private ImageView postImageView, postLikeBtn, postCommentBtn;
        private TextView titleView, descView, postDateView, postUserName, postLikeCount;

        private CircleImageView postUserImageView;
        private ImageButton postDeleteBtn;

        private CardView postCardView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            postCardView = mView.findViewById(R.id.main_post);
            postLikeBtn = mView.findViewById(R.id.post_like_btn);
            postCommentBtn = mView.findViewById(R.id.post_comment_btn);
            postDeleteBtn = mView.findViewById(R.id.post_delete_btn);
        }

        public void setTitleText(String titleText) {
            titleView = mView.findViewById(R.id.post_title);
            titleView.setText(titleText);
        }

        public void setDescText(String descText) {
            descView = mView.findViewById(R.id.post_desc);
            descView.setText(descText);
        }

        public void setPostImage(String downloadUri, String downloadThumbUri) {
            postImageView = mView.findViewById(R.id.post_image);

            RequestOptions placeholderOptions = new RequestOptions();
            placeholderOptions.placeholder(R.drawable.image_placeholder);

            Glide.with(context)
                    .applyDefaultRequestOptions(placeholderOptions)
                    .load(downloadUri)
                    .thumbnail(Glide.with(context)
                            .load(downloadThumbUri))
                    .into(postImageView);
        }

        public void setDate(String date) {
            postDateView = mView.findViewById(R.id.post_date);
            postDateView.setText(date);
        }

        public void setUserData(String name, String image) {
            postUserName = mView.findViewById(R.id.post_username);
            postUserImageView = mView.findViewById(R.id.post_user_image);

            postUserName.setText(name);

            RequestOptions placeholderOptions = new RequestOptions();
            placeholderOptions.placeholder(R.drawable.profile_placeholder);

            Glide.with(context).applyDefaultRequestOptions(placeholderOptions).load(image).into(postUserImageView);
        }

        public void updateLikesCount(int count) {
            postLikeCount = mView.findViewById(R.id.post_like_count);
            postLikeCount.setText(count + " Likes");
        }
    }
}
