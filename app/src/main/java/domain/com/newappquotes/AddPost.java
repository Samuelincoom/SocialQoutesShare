package domain.com.newappquotes;

/*----------------------------------

    - InstaQuotes -

    Created by cubycode @2017
    All Rights reserved

-----------------------------------*/


import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class AddPost extends AppCompatActivity implements View.OnClickListener {

    /* Views */
    EditText quoteTxt;


    /* Variables */
    int colorInt = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_post);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();


        // Init views
        quoteTxt = findViewById(R.id.apQuoteTxt);
        quoteTxt.setTypeface(Configs.bebasFont);


        colorInt = 0;


        // Show current User name and avatar
        ParseUser currUser = ParseUser.getCurrentUser();
        TextView fnTxt = findViewById(R.id.oupFullnameTxt);
        fnTxt.setText(currUser.getString(Configs.USER_FULLNAME));

        // Get Avatar
        final ImageView myImage = findViewById(R.id.oupAvatarImg);
        ParseFile fileObject = (ParseFile)currUser.get(Configs.USER_AVATAR);
        if (fileObject != null ) {
            fileObject.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            myImage.setImageBitmap(bmp);
        }}}});}





        // MARK: - INITIALIZE COLOR BUTTONS ------------------------------------------
        for (int i = 0; i< Configs.colorsArray.length; i++) {
            LinearLayout layout =  findViewById(R.id.apColorsLayout);
            layout.setOrientation(LinearLayout.HORIZONTAL);

            // Setup the Buttons
            Button colorButt = new Button(this);
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44, getResources().getDisplayMetrics());
            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44, getResources().getDisplayMetrics());

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
            layoutParams.setMargins(5, 0, 0, 0);
            colorButt.setLayoutParams(layoutParams);
            colorButt.setId(i);
            colorButt.setBackgroundColor( Color.parseColor(Configs.colorsArray[i]) );
            colorButt.setOnClickListener(this);

            //add button to the layout
            layout.addView(colorButt);
        }





        // MARK: - POST QUOTE BUTTON ------------------------------------
        Button postButt = findViewById(R.id.apPostButt);
        postButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              Configs.showPD("Please wait...", AddPost.this);
              dismissKeyboard();

              ParseObject pObj = new ParseObject(Configs.POSTS_CLASS_NAME);

              // Save data
              pObj.put(Configs.POSTS_USER_POINTER,ParseUser.getCurrentUser() );
              pObj.put(Configs.POSTS_QUOTE, quoteTxt.getText().toString() );
              pObj.put(Configs.POSTS_COLOR, colorInt);
              pObj.put(Configs.POSTS_IS_REPORTED, false);

              // Add keywords
              List<String> keywords = new ArrayList<String>();
              String[] one = quoteTxt.getText().toString().toLowerCase().split(" ");
              for (String keyw : one) { keywords.add(keyw); }
              pObj.put(Configs.POSTS_KEYWORDS, keywords);

              if (quoteTxt.getText().toString().matches("") ) {
                  Configs.hidePD();
                  Configs.simpleAlert("You must type something!", AddPost.this);

              } else {
                  // Saving block
                  pObj.saveInBackground(new SaveCallback() {
                      @Override
                      public void done(ParseException e) {
                          if (e == null) {
                              Configs.hidePD();
                              finish();
                          } else {
                              Configs.hidePD();
                              Configs.simpleAlert(e.getMessage(), AddPost.this);
                  }}});

          }}});






        // MARK: - DISMISS BUTTON ------------------------------------
        Button dismButt = findViewById(R.id.apDismissButt);
        dismButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              finish();
         }});


    }// end onCreate()






    // MARK: - SET QUOTE BACKGROUND COLOR
    @Override
    public void onClick(View v) {
        quoteTxt.setBackgroundColor(Color.parseColor(Configs.colorsArray[v.getId()]));
        colorInt = v.getId();
        Log.i("log-", "COLOR: " + colorInt);
    }





    // MARK: - DISMISS KEYBOARD
    public void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(quoteTxt.getWindowToken(), 0);
    }


}// @end
