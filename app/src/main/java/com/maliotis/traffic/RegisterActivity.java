package com.maliotis.traffic;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    EditText email,password;
    Button register;
    FirebaseAuth mFirebaseAuth;
    TextView login;
    String sEmail;
    String sPassword;
    FirebaseUser user;
    String uid;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mUserId ;
    DatabaseReference mEmail;
    DatabaseReference mChild = mRootRef.child("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirebaseAuth = FirebaseAuth.getInstance();
        email = (EditText) findViewById(R.id.editTextRegisterEmail);
        password = (EditText) findViewById(R.id.editTextRegisterPassword);
        login = (TextView) findViewById(R.id.textViewLogin);

        register = (Button) findViewById(R.id.buttonRegister);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent getMeToLogin = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(getMeToLogin);
            }
        });

    }

    void registerUser() {
        sEmail = email.getText().toString();
        sPassword = password.getText().toString();
        if(TextUtils.isEmpty(sEmail)){
            // Email is empty
            Toast.makeText(this, "Email is empty",Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(sPassword)){
            // Password is empty
            Toast.makeText(this,"Password is empty",Toast.LENGTH_SHORT).show();
            return;
        }


        // if registration is ok show a progress bar
        Toast.makeText(RegisterActivity.this,"Registering User...",Toast.LENGTH_SHORT);

        // Register User with email and password
        mFirebaseAuth.createUserWithEmailAndPassword(sEmail,sPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this,"Registered successfully",Toast.LENGTH_SHORT).show();
                            user = mFirebaseAuth.getCurrentUser();
                            uid = user.getUid();
                            mUserId = mChild.child(uid);
                            mEmail = mUserId.child("email");
                            //mUserId.setValue(uid);
                            mEmail.setValue(user.getEmail());

                        }
                        else{
                            Toast.makeText(RegisterActivity.this,"Could not register, please try again!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }



}
