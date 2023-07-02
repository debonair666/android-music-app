package com.example.music;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        UserDatebaseHelper userDatebaseHelper = new UserDatebaseHelper(this, "user.db", 1);
        final SQLiteDatabase userDatabase = userDatebaseHelper.getWritableDatabase();

        final EditText username = findViewById(R.id.username);
        final EditText password = findViewById(R.id.password);
        final EditText email = findViewById(R.id.email);
        final Button register = findViewById(R.id.register_register);

        register.setOnClickListener(v -> {
            String usernameValue = username.getText().toString();
            String passwordValue = password.getText().toString();
            String emailValue = email.getText().toString();
            if (usernameValue.equals("") || passwordValue.equals("") || emailValue.equals("")) {
                Toast.makeText(this, "个人信息请勿为空", Toast.LENGTH_SHORT).show();
            } else {
                ContentValues contentValues = new ContentValues();
                contentValues.put("username", usernameValue);
                contentValues.put("password", passwordValue);
                contentValues.put("email", emailValue);
                if (userDatabase.insert("user", null, contentValues) == -1) {
                    Toast.makeText(this, "用户名已被注册", Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                }
            }
        });


    }
}