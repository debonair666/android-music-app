package com.example.music;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final EditText username = findViewById(R.id.username);
        final EditText password = findViewById(R.id.password);
        final Button login = findViewById(R.id.login_login);
        final Button register = findViewById(R.id.login_register);
        final CheckBox rememberPassword = findViewById(R.id.rememberPassword);
        //获取上次输入的用户名，若没有则为空
        String savedUsername = getPreferences(0).getString("username", "");
        username.setText(savedUsername);

        boolean isRememberPassword = getPreferences(0).getBoolean("isRememberPassword", false);
        rememberPassword.setChecked(isRememberPassword);

        if (isRememberPassword) {
            password.setText(getPreferences(0).getString("password", ""));
        }


        login.setOnClickListener(v -> {

            String usernameValue = username.getText().toString();
            String passwordValue = password.getText().toString();

            //保存用户名
            SharedPreferences.Editor editor = getPreferences(0).edit();
            editor.putString("username", usernameValue);

            String[] selectionArgs = {usernameValue, passwordValue};
            UserDatebaseHelper userDatebaseHelper = new UserDatebaseHelper(this, "user.db", 1);
            SQLiteDatabase userDatabase = userDatebaseHelper.getWritableDatabase();
            Cursor cursor = userDatabase.rawQuery("select * from user where username=? and password=?", selectionArgs);

            if (cursor.moveToFirst()) {

                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                editor.putString("email", email);
                //保存密码
                if (rememberPassword.isChecked()) {
                    editor.putString("password", passwordValue);
                    editor.putBoolean("isRememberPassword", true);
                } else {
                    editor.putString("password", "");
                    editor.putBoolean("isRememberPassword", false);
                }
                editor.apply();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                password.setText("");
                editor.apply();
            }
        });

        register.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}