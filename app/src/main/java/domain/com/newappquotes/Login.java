package domain.com.newappquotes;

/*----------------------------------

    - InstaQuotes -

    Created by cubycode @2017
    All Rights reserved

-----------------------------------*/

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

public class Login extends AppCompatActivity {

    /* Views */
    EditText usernameTxt;
    EditText passwordTxt;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();



        // Init views
        usernameTxt = findViewById(R.id.usernameTxt);
        passwordTxt = findViewById(R.id.passwordTxt);



        // Login Button
        Button loginButt = findViewById(R.id.loginButt);
        loginButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Configs.showPD("Please wait...", Login.this);

                ParseUser.logInInBackground(usernameTxt.getText().toString(), passwordTxt.getText().toString(),
                        new LogInCallback() {
                            public void done(ParseUser user, ParseException error) {
                                if (user != null) {
                                    Configs.hidePD();
                                    startActivity(new Intent(Login.this, Home.class));
                                } else {
                                    Configs.hidePD();
                                    Configs.simpleAlert(error.getMessage(), Login.this);
                }}});

            }});



        // Sign Up Button
        Button signupButt = findViewById(R.id.signUpButt);
        signupButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, SignUp.class));
        }});



        // Forgot Password Button
        Button fpButt = findViewById(R.id.forgotPassButt);
        fpButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(Login.this);
                        alert.setTitle(R.string.app_name);
                        alert.setMessage("Type the valid email address you've used to register on this app");

                        // Add an EditTxt
                        final EditText editTxt = new EditText (Login.this);
                        editTxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                        alert.setView(editTxt);

                        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                // Reset password
                                ParseUser.requestPasswordResetInBackground(editTxt.getText().toString(), new RequestPasswordResetCallback() {
                                    public void done(ParseException error) {
                                        if (error == null) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                                            builder.setMessage("We've sent you an email to reset your password!")
                                                    .setTitle(R.string.app_name)
                                                    .setPositiveButton("OK", null);
                                            AlertDialog dialog = builder.create();
                                            dialog.setIcon(R.drawable.logo);
                                            dialog.show();

                                        } else {
                                            Configs.simpleAlert(error.getMessage(), Login.this);
                                        }
                                    }
                                });

                            }});

                        alert.setNegativeButton("Cancel", null);
                        alert.show();

                    }});


            }// end onCreate()






        }//@end
