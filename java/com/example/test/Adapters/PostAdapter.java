package com.example.test.Adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.LoginAndProfile.OtherProfileActivity;
import com.example.test.Models.PostModel;
import com.example.test.R;
import com.example.test.RestaurantSearch.PlacesDetails;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyHolder> {

    Context context;
    List<PostModel> postList;

    String myUid;
    private DatabaseReference postsRef;


    public PostAdapter(Context context, List<PostModel> postList){
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_xml, viewGroup, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, final int i) {
        final String currplaceid = postList.get(i).getPlaceId();
        String currplace = postList.get(i).getCurrentplace();
        final String uid = postList.get(i).getUid();
        String userimage = postList.get(i).getImage();
        final String pid = postList.get(i).getPid();
        final String postimage = postList.get(i).getPostimage();
        String username = postList.get(i).getPostname();
        String posttime = postList.get(i).getPosttime();
        String posttitle = postList.get(i).getPosttitle();
        String postdescription = postList.get(i).getPostdescription();


        //convert timestamp to following structure, act as post id
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(posttime));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar).toString();

        //set data
        myHolder.userName.setText(username);
        myHolder.postTitle.setText(posttitle);
        myHolder.currPlace.setText(currplace);
        myHolder.postDescription.setText(postdescription);
        myHolder.postImage.setVisibility(View.VISIBLE);

        //set post image
        if(postimage.equals("noImage")){
            //hide image view
            myHolder.postImage.setVisibility(View.GONE);
        }else{
            try {
                Picasso.get().load(postimage).into(myHolder.postImage);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        //set user profile pic
        try {
            Picasso.get().load(userimage).into(myHolder.userImage);
        }catch (Exception e){
            e.printStackTrace();
        }

        //handle button click listeners
        myHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] options = {"Yes", "No"};
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirm Delete?");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            deletePost(myHolder.deleteBtn, uid, myUid, pid, postimage);
                        }
                    }
                });
                builder.create().show();
            }
        });

        myHolder.currPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detailsIntent = new Intent(context, PlacesDetails.class);
                detailsIntent.putExtra("id", currplaceid);
                context.startActivity(detailsIntent);
            }
        });

        myHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        myHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //implements later
            }
        });

        myHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //implements later
            }
        });

        myHolder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, OtherProfileActivity.class);
                i.putExtra("uid", uid);
                context.startActivity(i);
            }
        });
    }


    private void deletePost(Button deleteBtn, final String uid, final String myUid, final String pid, final String postimage) {
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uid.equals(myUid)) {
                    beginDelete(pid, postimage);
                }
            }
        });
    }

    private void beginDelete(String pid, String postimage) {
        if(postimage.equals("noImage")){
            deleteWithoutImage(pid);
        }else{
            deleteWithImage(pid, postimage);
        }
    }

    private void deleteWithImage(final String pid, String postimage) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(postimage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pid").equalTo(pid);
                fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(context, "Post deleted successfully." ,Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage(String pid) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pid").equalTo(pid);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView userImage, postImage;
        TextView userName, postTime, postTitle, postLikes, postDescription, currPlace;
        Button deleteBtn;
        Button likeBtn, commentBtn, shareBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView){
            super(itemView);

            userImage = itemView.findViewById(R.id.userImage);
            postImage = itemView.findViewById(R.id.postImage);
            currPlace = itemView.findViewById(R.id.currlocation);
            userName = itemView.findViewById(R.id.username);
            postTime = itemView.findViewById(R.id.postTime);
            postTitle = itemView.findViewById(R.id.postTitle);
            postLikes = itemView.findViewById(R.id.postLikes);
            postDescription = itemView.findViewById(R.id.postDesc);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            profileLayout = itemView.findViewById(R.id.profilelayout);
        }
    }
}
