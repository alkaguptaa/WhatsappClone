package com.chatz.whatsapp.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.chatz.whatsapp.R;
import com.chatz.whatsapp.Utils.Const;
import com.chatz.whatsapp.model.NewsFeed;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CreateNewsFeedActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private Button btnCreateFeed;
    private Button btnChooseImage;
    private EditText etMessage;
    private ImageView imgSelected;

    private Uri actualFileUri;
    private Uri filePathUri;
    private String filePathString = "";
    private static final int PICK_CAMERA = 111;
    private static final int REQUEST_CODE_CAPTURE = 222;
    private static final int PICK_IMAGE = 333;
    private static final int CODE_IMAGE_PICKER = 444;

    private ProgressDialog progressDialog;

    private NewsFeed newsFeed = new NewsFeed();

    private String currentUserID;
    private String currentUserName;
    private String currentUserAvatar;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference usersRef;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_news_feed_activity);

        setToolbar();

        initView();

        setOnClickListner();

    }

    private void setOnClickListner() {
        btnCreateFeed.setOnClickListener(this);
        btnChooseImage.setOnClickListener(this);
    }

    private void setToolbar() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    private void initView() {
        btnCreateFeed = findViewById(R.id.btnCreateFeed);
        etMessage = findViewById(R.id.etMessage);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        imgSelected = findViewById(R.id.imgSelected);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        usersRef = FirebaseDatabase.getInstance().getReference();
        rootRef = FirebaseDatabase.getInstance().getReference();

        getUserInfo();
    }

    private void getUserInfo() {
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

    private void checkForPermissionAndOpenCamera() {

        final String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};

        final List<String> permissionsToRequest = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[permissionsToRequest.size()]), PICK_CAMERA);
        } else {
            startCamera();
        }

    }

    private void startCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Const.getOutputMediaFile(this) != null) {
            actualFileUri = Uri.fromFile(Const.getOutputMediaFile(this));
            Uri camerafileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".Utils.AppFileProvider", Const.getOutputMediaFile(this));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, camerafileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_CODE_CAPTURE);
        }

    }

    private void checkForPermissionAndOpenGallery() {

        final String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

        final List<String> permissionsToRequest = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[permissionsToRequest.size()]), PICK_IMAGE);
        } else {
            Const.intentForPhotos(this, CODE_IMAGE_PICKER, null);
        }

    }


    private void chooseDialogViewOrEdit() {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

        String[] list = new String[]{"Camera", "Gallery", "Cancel"};

        final String[] finalList = list;


        builder.setItems(list, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch (finalList[which]) {

                    case "Camera":
                        checkForPermissionAndOpenCamera();
                        break;

                    case "Gallery":
                        checkForPermissionAndOpenGallery();
                        break;

                    default:
                        break;
                }
            }
        });

        android.app.AlertDialog dialog = builder.create();

        dialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_CODE_CAPTURE:
                    if (actualFileUri != null) {
                        new CaptureImageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
                    }
                    break;
                case CODE_IMAGE_PICKER:

                    funImagePickerResult(data);

                    break;
                default:
                    break;
            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CAPTURE) {
            if (actualFileUri != null) {
                new CaptureImageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
            }
        }
    }

    private void funImagePickerResult(Intent data) {
        if (data != null) {
            final Uri uri = data.getData();
            String realPath = Const.convertUriToRealPath(this, uri);

            if (realPath != null && !realPath.isEmpty() && new File(realPath).isFile()) {

                Uri setURI = Uri.fromFile(new File(realPath));

                filePathString = realPath;
                filePathUri = setURI;
                btnChooseImage.setText("Remove Image");

                imgSelected.setImageURI(setURI);
                Log.v("path", new File(realPath).getAbsolutePath());
            }
        }
    }

    private class CaptureImageTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            try {

                Uri setURI = Uri.parse(actualFileUri.getEncodedPath());

                return Const.getCompressFilePath(new File(Const.getRealPathFromURI(CreateNewsFeedActivity.this, setURI)).getAbsolutePath(), System.currentTimeMillis() + "");
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }

        }

        @Override
        protected void onPostExecute(String newFilePath) {
            super.onPostExecute(newFilePath);

            Uri setURI = Uri.fromFile(new File(newFilePath));

            filePathString = newFilePath;
            filePathUri = setURI;
            btnChooseImage.setText("Remove Image");

            imgSelected.setImageURI(setURI);

        }
    }

    private void uploadImageToFirebase() {
        if (filePathUri != null) {

            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("NewsFeed images/" + UUID.randomUUID().toString());

            ref.putFile(filePathUri)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful()) {
                                                String generatedFilePath = task.getResult().toString();

                                                Calendar calendar = Calendar.getInstance();

                                                SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
                                                String saveCurrentDate = currentDate.format(calendar.getTime());

                                                SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                                                String saveCurrentTime = currentTime.format(calendar.getTime());

                                                newsFeed.setMessage(etMessage.getText().toString().trim());
                                                newsFeed.setImage(generatedFilePath);
                                                newsFeed.setTime(saveCurrentTime);
                                                newsFeed.setDate(saveCurrentDate);
                                                newsFeed.setSenderName(currentUserName);
                                                newsFeed.setSenderAvatar(currentUserAvatar);
                                                newsFeed.setSenderID(currentUserID);

                                                createFeedTask();
                                            }
                                        }
                                    });

                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(CreateNewsFeedActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                                }
                            });
        }else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void createFeedTask() {
        String feedID = rootRef.child("NewsFeed").push().getKey();
        newsFeed.setId(feedID);
        rootRef.child("NewsFeed").child(feedID).setValue(newsFeed).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Toast.makeText(CreateNewsFeedActivity.this, "Feed post successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    // error
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCreateFeed:
                if (etMessage.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
                } else if (filePathString == null || filePathString.isEmpty() || filePathString.equalsIgnoreCase("null")) {
                    Toast.makeText(this, "Please select or capture valid image", Toast.LENGTH_SHORT).show();
                } else {
                    uploadImageToFirebase();
                }
                break;
            case R.id.btnChooseImage:
                if (btnChooseImage.getText().toString().equalsIgnoreCase("Remove Image")) {
                    filePathString = "";
                    filePathUri = null;
                    btnChooseImage.setText("Choose image");
                    imgSelected.setImageResource(0);
                } else {
                    chooseDialogViewOrEdit();
                }
                break;
            default:
                break;
        }
    }
}