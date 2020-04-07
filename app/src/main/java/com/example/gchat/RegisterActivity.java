package com.example.gchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText aEmailet,aPasswordet;
    Button aRegisterbtn;
    ProgressDialog progressDialog;
    TextView have_account;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Create Account");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        aEmailet=findViewById(R.id.email_et);
        aPasswordet=findViewById(R.id.password_et);
        aRegisterbtn=findViewById(R.id.register);
        have_account=findViewById(R.id.have_accountTv);

        mAuth=FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Registering User...");
        progressDialog.setMessage("Please Wait...");

        aRegisterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=aEmailet.getText().toString().trim();
                String password= aPasswordet.getText().toString().trim();

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    aEmailet.setError("Invalid Email");
                    aEmailet.setFocusable(true);
                }
                else if(!email.substring(email.length()-10).equals("@gchat.com"))
                {
                    aEmailet.setError("Email format example: xyz@gchat.com" );
                    aEmailet.setFocusable(true);
                }
                else if(password.length()<6){
                    aPasswordet.setError("Password length should be at least 6 characters");
                    aPasswordet.setFocusable(true);
                }
                else {
                    //Toast.makeText(RegisterActivity.this,"registered successfully",Toast.LENGTH_SHORT).show();
                    registerUser(email,password);
                }

            }
        });

        have_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });

    }

    private void registerUser(String email, String password) {
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            String email=user.getEmail();
                            String uid=user.getUid();

                            HashMap<Object,String> hashMap=new HashMap<>();
                            hashMap.put("email",email);
                            hashMap.put("uid",uid);
                            hashMap.put("name","Update your name");
                            hashMap.put("onlineStatus","Online");
                            hashMap.put("typingTo","noOne");
                            hashMap.put("phone","Update your phone no.");
                            hashMap.put("image","");
                            hashMap.put("cover","");
                            //firebase database instance
                            FirebaseDatabase database=FirebaseDatabase.getInstance();
                            //path to store user data in "Users"
                            DatabaseReference reference=database.getReference("Users");
                            reference.child(uid).setValue(hashMap);

                            Toast.makeText(RegisterActivity.this, "Registered Successfully...",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            progressDialog.dismiss();

                            Toast.makeText(RegisterActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
