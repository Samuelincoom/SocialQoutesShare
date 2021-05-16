package domain.com.newappquotes;

/*----------------------------------

    - InstaQuotes -

    Created by cubycode @2017
    All Rights reserved

-----------------------------------*/

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();


        // Init TabBar buttons
        Button tab_one = findViewById(R.id.tab_one);
        Button tab_two = findViewById(R.id.tab_two);

        tab_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(About.this, Home.class));
        }});

        tab_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(About.this, Me.class));
        }});





        // MARK: - WEBSITE BUTTON ------------------------------------
        final Button webButt = findViewById(R.id.abWebsiteButt);
        webButt.setText(Configs.WEBSITE_URL);
        webButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              startActivity( new Intent(Intent.ACTION_VIEW, Uri.parse(Configs.WEBSITE_URL) ));
        }});




        // MARK: - EMAIL BUTTON ------------------------------------
        Button emButt = findViewById(R.id.abEmailButt);
        emButt.setText(Configs.ADMIN_EMAIL_ADDRESS);
        emButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              Intent emailIntent = new Intent(Intent.ACTION_SEND);
              emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              emailIntent.setType("message/rfc822");
              emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {Configs.ADMIN_EMAIL_ADDRESS });
              emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Contact request");
              emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello,\n");
              startActivity(Intent.createChooser(emailIntent, "Send mail using..."));
         }});



        // MARK: - FACEBOOK BUTTON ------------------------------------
        Button fbButt = findViewById(R.id.abFacebookButt);
        fbButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              startActivity( new Intent(Intent.ACTION_VIEW, Uri.parse(Configs.FACEBOOK_URL) ));
        }});




        // MARK: - TWITTER BUTTON ------------------------------------
        Button twButt = findViewById(R.id.abTwitterButt);
        twButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity( new Intent(Intent.ACTION_VIEW, Uri.parse(Configs.TWITTER_URL) ));
        }});



    }// end onCreate()



}// @end
