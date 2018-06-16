package sg.com.kaplan.android.redcloud;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView postListView;
    private List<Post> postList;
    private List<User> userList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private PostRecyclerAdapter postRecyclerAdapter;

    private DocumentSnapshot lastVisible;
    private Boolean firstLoad = true;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        postList = new ArrayList<>();
        userList = new ArrayList<>();
        postListView = view.findViewById(R.id.post_list_view);

        mAuth = FirebaseAuth.getInstance();

        postRecyclerAdapter = new PostRecyclerAdapter(postList, userList);
        postListView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        postListView.setAdapter(postRecyclerAdapter);

        if (mAuth.getCurrentUser() != null) {

            db = FirebaseFirestore.getInstance();

            postListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if (reachedBottom) {
                        // Toast.makeText(container.getContext(), "Reached bottom", Toast.LENGTH_SHORT).show();
                        loadPost();
                    }
                }
            });

            // Sort results by descending (latest posts first)
            Query firstQuery = db.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(10);

            // Set real time database listener
            firstQuery.addSnapshotListener(Objects.requireNonNull(getActivity()), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshots != null) {
                        if (!documentSnapshots.isEmpty()) {

                            if (firstLoad) {
                                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                postList.clear();
                                userList.clear();
                            }

                            for (DocumentChange doc: documentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String newPostId = doc.getDocument().getId();
                                    final Post newPost = doc.getDocument().toObject(Post.class).withId(newPostId);

                                    String postUserId = doc.getDocument().getString("user_id");

                                    db.collection("Users").document(postUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {

                                                User user = task.getResult().toObject(User.class);

                                                if (firstLoad) {
                                                    userList.add(user);
                                                    postList.add(newPost);
                                                } else {
                                                    userList.add(0, user);
                                                    postList.add(0, newPost);
                                                }

                                            }

                                            postRecyclerAdapter.notifyDataSetChanged();
                                        }
                                    });


                                }
                            }

                            firstLoad = false;
                        }
                    }
                }
            });
        }



        // Inflate the layout for this fragment
        return view;
    }

    public void loadPost() {
        if (mAuth.getCurrentUser() != null) {
            // Sort results by descending (latest posts first)
            Query nextQuery = db.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(10);

            // Set real time database listener
            nextQuery.addSnapshotListener(getActivity() ,new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshots != null) {
                        if (!documentSnapshots.isEmpty()) {
                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            for (DocumentChange doc: documentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String newPostId = doc.getDocument().getId();
                                    final Post newPost = doc.getDocument().toObject(Post.class).withId(newPostId);

                                    String postUserId = doc.getDocument().getString("user_id");

                                    db.collection("Users").document(postUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {

                                                User user = task.getResult().toObject(User.class);

                                                userList.add(user);
                                                postList.add(newPost);

                                            }

                                            postRecyclerAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            });
        }
    }

}
