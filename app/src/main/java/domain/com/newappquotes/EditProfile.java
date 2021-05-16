package domain.com.newappquotes;

/*----------------------------------

    - InstaQuotes -

    Created by cubycode @2017
    All Rights reserved

-----------------------------------*/


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class EditProfile extends AppCompatActivity {

    /* Views */
    EditText fnTxt;
    TextView emailTxt;
    ImageView avatarImg;


    /* Variables */
    MarshMallowPermission mmp = new MarshMallowPermission(this);





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();



        // Init views
        fnTxt = findViewById(R.id.epFullnameTxt);
        emailTxt = findViewById(R.id.epEmailTxt);
        avatarImg = findViewById(R.id.epAvatarImg);

        // Call query
        showUserDetails();



        // MARK: - UPLOAD AVATAR ------------------------------------------
        avatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alert  = new AlertDialog.Builder(EditProfile.this);
                alert.setTitle("SELECT SOURCE")
                .setIcon(R.drawable.logo)
                .setItems(new CharSequence[] {
                                "Take a picture",
                                "Pick from Gallery" },
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    // Open Camera
                                    case 0:
                                        if (!mmp.checkPermissionForCamera()) {
                                            mmp.requestPermissionForCamera();
                                        } else {
                                            openCamera();
                                        }
                                        break;

                                    // Open Gallery
                                    case 1:
                                        if (!mmp.checkPermissionForReadExternalStorage()) {
                                            mmp.requestPermissionForReadExternalStorage();
                                        } else {
                                            openGallery();
                                        }
                                        break;
                        }}})
                .setNegativeButton("Cancel", null);
                alert.create().show();
        }});




        // MARK: - UPDATE PROFILE BUTTON ------------------------------------
        Button upButt = findViewById(R.id.epUpdateButt);
        upButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              dismissKeyboard();
              Configs.showPD("Please wait...", EditProfile.this);

              ParseUser currUser = ParseUser.getCurrentUser();

              // Save full name
              currUser.put(Configs.USER_FULLNAME, fnTxt.getText().toString());

              // Save avatar
              Bitmap bitmap = ((BitmapDrawable) avatarImg.getDrawable()).getBitmap();
              ByteArrayOutputStream stream = new ByteArrayOutputStream();
              bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
              byte[] byteArray = stream.toByteArray();
              ParseFile imageFile = new ParseFile("avatar.jpg", byteArray);
              currUser.put(Configs.USER_AVATAR, imageFile);

              // Saving block
              currUser.saveInBackground(new SaveCallback() {
                   @Override
                   public void done(ParseException e) {
                      if (e == null) {
                          Configs.hidePD();

                          Configs.simpleAlert("Your profile has been updated!", EditProfile.this);
                      // error
                      } else {
                          Configs.hidePD();
                          Configs.simpleAlert(e.getMessage(), EditProfile.this);
              }}});

         }});





        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = findViewById(R.id.epBackButt);
        backButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              finish();
        }});



    }// end onCreate()






    // MARK: - SHOW USER DETAILS ----------------------------------------------------------------
    void showUserDetails() {
        ParseUser currUser = ParseUser.getCurrentUser();

        fnTxt.setText(currUser.getString(Configs.USER_FULLNAME));
        emailTxt.setText(currUser.getString(Configs.USER_EMAIL));

        // Get Image
        ParseFile fileObject = (ParseFile)currUser.get(Configs.USER_AVATAR);
        if (fileObject != null ) {
            fileObject.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            avatarImg.setImageBitmap(bmp);
        }}}});}

    }






    // IMAGE HANDLING METHODS ------------------------------------------------------------------------
    int CAMERA = 0;
    int GALLERY = 1;
    Uri imageURI;
    File file;


    // OPEN CAMERA
    public void openCamera() {
        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(Environment.getExternalStorageDirectory(), "image.jpg");
        imageURI = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
        startActivityForResult(intent, CAMERA);
    }


    // OPEN GALLERY
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), GALLERY);
    }



    // IMAGE PICKED DELEGATE -----------------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Bitmap bm = null;

            // Image from Camera
            if (requestCode == CAMERA) {

                try {
                    File f = file;
                    ExifInterface exif = new ExifInterface(f.getPath());
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                    int angle = 0;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) { angle = 90; }
                    else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) { angle = 180; }
                    else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) { angle = 270; }
                    Log.i("log-", "ORIENTATION: " + orientation);

                    Matrix mat = new Matrix();
                    mat.postRotate(angle);

                    Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, null);
                    bm = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);
                }
                catch (IOException | OutOfMemoryError e) { Log.i("log-", e.getMessage()); }


                // Image from Gallery
            } else if (requestCode == GALLERY) {
                try { bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (IOException e) { e.printStackTrace(); }
            }


            // Set image
            Bitmap scaledBm = Configs.scaleBitmapToMaxSize(300, bm);
            avatarImg.setImageBitmap(scaledBm);
        }
    }
    //---------------------------------------------------------------------------------------------







    // MARK: - DISMISS KEYBOARD
    public void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(fnTxt.getWindowToken(), 0);
    }


}// @end
