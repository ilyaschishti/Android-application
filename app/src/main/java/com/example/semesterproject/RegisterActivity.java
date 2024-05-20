package com.example.semesterproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "RegisterActivity";

    private EditText editTextEmail, editTextPassword, editTextName, editTextMobile;
    ImageView imageViewProfile;
    Button buttonUploadProfile, buttonRegister;
    private Uri profileImageUri;

    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextName = findViewById(R.id.editTextName);
        editTextMobile = findViewById(R.id.editTextMobile);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        buttonUploadProfile = findViewById(R.id.buttonUploadProfile);
        buttonRegister = findViewById(R.id.buttonRegister);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference("profile_pictures");

        buttonUploadProfile.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        buttonRegister.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String name = editTextName.getText().toString().trim();
            String mobile = editTextMobile.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name) || TextUtils.isEmpty(mobile)) {
                Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(email, password, name, mobile);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            imageViewProfile.setImageURI(profileImageUri);
        }
    }

    private void registerUser(String email, String password, String name, String mobile) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();

                            Map<String, Object> userdata = new HashMap<>();
                            userdata.put("name", name);
                            userdata.put("email", email);
                            userdata.put("mobile", mobile);

                            db.collection("users").document(userId).set(userdata)
                                    .addOnSuccessListener(aVoid -> {
                                        if (profileImageUri != null) {
                                            uploadImageToFirebase(profileImageUri, userId);
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w(TAG, "Failed to add customer data in database", e);
                                        Toast.makeText(RegisterActivity.this, "Failed to add customer data", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Authentication failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImageToFirebase(Uri imageUri, String userId) {
        StorageReference fileRef = mStorage.child(userId + ".jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                db.collection("users").document(userId).update("profilePictureUrl", uri.toString())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(RegisterActivity.this, "Profile Picture Uploaded and Registration successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Failed to update profile picture URL in Firestore", e);
                            Toast.makeText(RegisterActivity.this, "Failed to update profile picture URL", Toast.LENGTH_SHORT).show();
                        });
            });
        }).addOnFailureListener(e -> {
            Log.w(TAG, "Profile Picture Upload Failed", e);
            Toast.makeText(RegisterActivity.this, "Profile Picture Upload Failed", Toast.LENGTH_SHORT).show();
        });
    }
}
