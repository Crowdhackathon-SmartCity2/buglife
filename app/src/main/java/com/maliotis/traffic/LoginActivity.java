package com.maliotis.traffic;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

public class LoginActivity extends AppCompatActivity {

    TextView registerTextView;
    Button loginButton;
    EditText emailEditText;
    EditText passwordEditText;
    String email;
    String password;
    FirebaseAuth mAuth;
    public FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        registerTextView = (TextView) findViewById(R.id.textViewRegister);
        loginButton = (Button) findViewById(R.id.buttonLogin);
        emailEditText = (EditText) findViewById(R.id.editTextEmail);
        passwordEditText = (EditText) findViewById(R.id.editTextPasswrod);
        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkEmailAndPassword()) {
                    loginUser();
                }
            }
        });

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent getMeToRegister = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(getMeToRegister);
            }
        });



    }
    private boolean checkEmailAndPassword(){
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();


        if (TextUtils.isEmpty(email)){
            Toast.makeText(LoginActivity.this,"Enter a valid email!",Toast.LENGTH_SHORT).show();
            return false;

        }
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(LoginActivity.this,"Enter a valid password!",Toast.LENGTH_SHORT).show();
            return false;
        }
        else
            return true;
    }

    private void loginUser() {

        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();

        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this,"Login was a succes...",Toast.LENGTH_SHORT).show();
                            user = mAuth.getCurrentUser();
                            Intent getToTaskActivity = new Intent(getApplicationContext(),MapsActivity.class);
                            Log.v("TAG","Before Starting Activity");
                            startActivity(getToTaskActivity);

                        }
                        else if(!task.isSuccessful()){
                            Toast.makeText(LoginActivity.this,"Login was not successfull please try again!",Toast.LENGTH_SHORT).show();


                        }
                    }
                });


    }
}
