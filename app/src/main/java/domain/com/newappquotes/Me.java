package domain.com.newappquotes;

/*----------------------------------

    - InstaQuotes -

    Created by cubycode @2017
    All Rights reserved

-----------------------------------*/

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class Me extends AppCompatActivity {

    /* Views */
    ListView quotesListView;
    RadioButton myQuotesRB, likedQuotesRB;


    /* Variables */
    List<ParseObject>postsArray;
    MarshMallowPermission mmp = new MarshMallowPermission(this);




    // ON START() ------------------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();

        // Call query
        showUserData();
    }





    // ON CREATE() ------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.me);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();




        // Init views
        quotesListView = findViewById(R.id.meQuotesListView);
        myQuotesRB = (RadioButton)findViewById(R.id.meMyQuotesRadioButt);
        likedQuotesRB = (RadioButton)findViewById(R.id.meLikedQuotesRadioButt);



        // Init TabBar buttons
        Button tab_one = findViewById(R.id.tab_one);
        Button tab_two = findViewById(R.id.tab_three);

        tab_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Me.this, Home.class));
        }});

        tab_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Me.this, About.class));
        }});




        // MARK: - MY QUOTES RADIO BUTTON ------------------------------------------
        myQuotesRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryMyPosts();
                likedQuotesRB.setChecked(false);
        }});



        // MARK: - LIKED QUOTES RADIO BUTTON ------------------------------------------
        likedQuotesRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryMyLikedQuotes();
                myQuotesRB.setChecked(false);
        }});




        // MARK: - ACTIVITY BUTTON ------------------------------------
        Button actButt = findViewById(R.id.meActivityButt);
        actButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              startActivity(new Intent(Me.this, ActivityScreen.class));
        }});




        // MARK: - EDIT PROFILE BUTTON ------------------------------------
        Button epButt = findViewById(R.id.meEditProfileButt);
        epButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              startActivity(new Intent(Me.this, EditProfile.class));
         }});




        // MARK: - LOGOUT BUTTON ------------------------------------
        Button logoutButt = findViewById(R.id.meLogoutButt);
        logoutButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              AlertDialog.Builder alert = new AlertDialog.Builder(Me.this);
              alert.setMessage("Are you sure you want to logout?")
                      .setTitle(R.string.app_name)
                      .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialogInterface, int i) {
                              Configs.showPD("Logging out...", Me.this);

                              ParseUser.logOutInBackground(new LogOutCallback() {
                                  @Override
                                  public void done(ParseException e) {
                                      Configs.hidePD();
                                      // Go Login activity
                                      startActivity(new Intent(Me.this, Login.class));
                              }});
                          }})
                      .setNegativeButton("Cancel", null)
                      .setIcon(R.drawable.logo);
              alert.create().show();
         }});



        // MARK: - POST QUOTE BUTTON ------------------------------------
        Button postButt = findViewById(R.id.mePostButt);
        postButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Me.this, AddPost.class));
        }});


    }// end onCreate()






    // MARK: - SHOW USER'S DATA ------------------------------------------------------------
    void showUserData() {
        ParseUser currUser = ParseUser.getCurrentUser();

        TextView fnTxt = findViewById(R.id.meFullnameTxt);
        fnTxt.setText(currUser.getString(Configs.USER_FULLNAME));

        // Get Image
        final ImageView avImage = findViewById(R.id.oupAvatarImg);
        ParseFile fileObject = currUser.getParseFile(Configs.USER_AVATAR);
        if (fileObject != null ) {
            fileObject.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            avImage.setImageBitmap(bmp);
        }}}});}


        // reset radio buttons
        myQuotesRB.setChecked(true);
        likedQuotesRB.setChecked(false);

        // Call query
        queryMyPosts();
    }






    // MARK: - QUERY MY POSTS ------------------------------------------------------
    void queryMyPosts() {
        ParseUser currUser = ParseUser.getCurrentUser();

        Configs.showPD("Please wait...", Me.this);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.POSTS_CLASS_NAME);
        query.whereEqualTo(Configs.POSTS_USER_POINTER, currUser);
        query.orderByDescending(Configs.POSTS_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    postsArray = objects;
                    Configs.hidePD();

                    class ListAdapter extends BaseAdapter {
                        private Context context;
                        public ListAdapter(Context context, List<ParseObject> objects) {
                            super();
                            this.context = context;
                        }

                        // CONFIGURE CELL
                        @Override
                        public View getView(final int position, View cell, ViewGroup parent) {
                            if (cell == null) {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                assert inflater != null;
                                cell = inflater.inflate(R.layout.cell_home, null);
                            }
                            final View finalCell = cell;

                            // Get Parse object
                            final ParseObject pObj = postsArray.get(position);

                            // Get userPointer
                            pObj.getParseObject(Configs.POSTS_USER_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(final ParseObject userPointer, final ParseException e) {

                                    // Get Quote text
                                    final TextView qTxt =  finalCell.findViewById(R.id.chQuoteTxt);
                                    qTxt.setText(pObj.getString(Configs.POSTS_QUOTE));
                                    qTxt.setTypeface(Configs.bebasFont);

                                    // Get Quote color
                                    int quoteColor = (int) pObj.getNumber(Configs.POSTS_COLOR);
                                    String colorStr = Configs.colorsArray[quoteColor];
                                    RelativeLayout cellBkg = (RelativeLayout) finalCell.findViewById(R.id.chCellBkg);
                                    cellBkg.setBackgroundColor(Color.parseColor(colorStr));
                                    qTxt.setBackgroundColor(Color.parseColor(colorStr));


                                    // Get Fullname
                                    final TextView fnTxt =  finalCell.findViewById(R.id.chFullnameTxt);
                                    fnTxt.setText(userPointer.getString(Configs.USER_FULLNAME));

                                    // Get Avatar
                                    final ImageView avImg =  finalCell.findViewById(R.id.chAvatarImg);
                                    ParseFile fileObject = (ParseFile) userPointer.get(Configs.USER_AVATAR);
                                    if (fileObject != null) {
                                        fileObject.getDataInBackground(new GetDataCallback() {
                                            public void done(byte[] data, ParseException error) {
                                                if (error == null) {
                                                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                    if (bmp != null) {
                                                        avImg.setImageBitmap(bmp);
                                    }}}});}



                                    // MARK: - SHARE QUOTE BUTTON --------------------------------------------------------
                                    Button shareButt =  finalCell.findViewById(R.id.chShareButt);
                                    shareButt.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {// Make a bitmap out of the TextView
                                            qTxt.buildDrawingCache();
                                            Bitmap txtBm = qTxt.getDrawingCache();

                                            if (!mmp.checkPermissionForWriteExternalStorage()) {
                                                mmp.requestPermissionForWriteExternalStorage();
                                            } else {
                                                Uri uri = getImageUri(Me.this, txtBm);
                                                Intent intent = new Intent(Intent.ACTION_SEND);
                                                intent.setType("image/jpeg");
                                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                                intent.putExtra(Intent.EXTRA_TEXT, "A nice quote by " +
                                                        fnTxt.getText().toString() + " on #InstaQuotes");
                                                startActivity(Intent.createChooser(intent, "Share on..."));
                                    }}});



                                    // MARK: - DELETE QUOTE BUTTON --------------------------------------------------------
                                    Button deleteButt =  finalCell.findViewById(R.id.chReportButt);
                                    deleteButt.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            AlertDialog.Builder alert = new AlertDialog.Builder(Me.this);
                                            alert.setMessage("Are you sure you want to delete this quote?")
                                                .setTitle(R.string.app_name)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        pObj.deleteInBackground(new DeleteCallback() {
                                                        @Override
                                                        public void done(ParseException e) {
                                                            // Remove selected row
                                                            postsArray.remove(position);
                                                            quotesListView.invalidateViews();
                                                            quotesListView.refreshDrawableState();
                                                    }});
                                                }})
                                                .setNegativeButton("Cancel", null)
                                                .setIcon(R.drawable.logo);
                                            alert.create().show();
                                    }});




                                    // MARK: - HIDDEN BUTTONS --------------------------------------------------------
                                    Button commButt =  finalCell.findViewById(R.id.chCommentButt);
                                    commButt.setVisibility(View.INVISIBLE);
                                    Button likeButt =  finalCell.findViewById(R.id.chLikeButt);
                                    likeButt.setVisibility(View.INVISIBLE);


                                }});// end userPointer

                            return cell;
                        }

                        @Override public int getCount() { return postsArray.size(); }
                        @Override public Object getItem(int position) { return postsArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }

                    // Set ListView adapter
                    quotesListView.setAdapter(new ListAdapter(Me.this, postsArray));


                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), Me.this);
        }}});
    }








    // MARK: - QUERY MY LIKES ------------------------------------------------------
    void queryMyLikedQuotes() {
        ParseUser currUser = ParseUser.getCurrentUser();

        Configs.showPD("Please wait...", Me.this);

        postsArray.clear();
        quotesListView.invalidateViews();
        quotesListView.refreshDrawableState();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.POSTS_CLASS_NAME);
        query.whereContains(Configs.POSTS_LIKED_BY, currUser.getObjectId());
        query.orderByAscending(Configs.POSTS_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    postsArray = objects;
                    Configs.hidePD();

                    class ListAdapter extends BaseAdapter {
                        private Context context;
                        public ListAdapter(Context context, List<ParseObject> objects) {
                            super();
                            this.context = context;
                        }

                        // CONFIGURE CELL
                        @Override
                        public View getView(final int position, View cell, ViewGroup parent) {
                            if (cell == null) {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                assert inflater != null;
                                cell = inflater.inflate(R.layout.cell_home, null);
                            }
                            final View finalCell = cell;

                            // Get Parse object
                            final ParseObject pObj = postsArray.get(position);

                            if (!pObj.getBoolean(Configs.POSTS_IS_REPORTED)) {

                                // Get userPointer
                                pObj.getParseObject(Configs.POSTS_USER_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                    public void done(final ParseObject userPointer, final ParseException e) {

                                        // Get Quote text
                                        final TextView qTxt =  finalCell.findViewById(R.id.chQuoteTxt);
                                        qTxt.setText(pObj.getString(Configs.POSTS_QUOTE));
                                        qTxt.setTypeface(Configs.bebasFont);

                                        // Get Quote color
                                        int quoteColor = (int) pObj.getNumber(Configs.POSTS_COLOR);
                                        String colorStr = Configs.colorsArray[quoteColor];
                                        RelativeLayout cellBkg = (RelativeLayout) finalCell.findViewById(R.id.chCellBkg);
                                        cellBkg.setBackgroundColor(Color.parseColor(colorStr));
                                        qTxt.setBackgroundColor(Color.parseColor(colorStr));


                                        // Get Fullname
                                        final TextView fnTxt =  finalCell.findViewById(R.id.chFullnameTxt);
                                        fnTxt.setText(userPointer.getString(Configs.USER_FULLNAME));

                                        // Get Avatar
                                        final ImageView avImg =  finalCell.findViewById(R.id.chAvatarImg);
                                        ParseFile fileObject = (ParseFile) userPointer.get(Configs.USER_AVATAR);
                                        if (fileObject != null) {
                                            fileObject.getDataInBackground(new GetDataCallback() {
                                                public void done(byte[] data, ParseException error) {
                                                    if (error == null) {
                                                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                        if (bmp != null) {
                                                            avImg.setImageBitmap(bmp);
                                        }}}});}

                                        // MARK: - TAP AVATAR -> SEE OTHER USER PROFILE -----------------------------------
                                        avImg.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent i = new Intent(Me.this, OtherUserProfile.class);
                                                Bundle extras = new Bundle();
                                                extras.putString("userID", userPointer.getObjectId());
                                                i.putExtras(extras);
                                                startActivity(i);
                                        }});





                                        // MARK: - COMMENT BUTTON --------------------------------------------------------
                                        Button commButt = finalCell.findViewById(R.id.chCommentButt);
                                        commButt.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent i = new Intent(Me.this, Comments.class);
                                                Bundle extras = new Bundle();
                                                extras.putString("objectID", pObj.getObjectId());
                                                i.putExtras(extras);
                                                startActivity(i);
                                        }});




                                        // MARK: - SHARE QUOTE BUTTON --------------------------------------------------------
                                        Button shareButt =  finalCell.findViewById(R.id.chShareButt);
                                        shareButt.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {// Make a bitmap out of the TextView
                                                qTxt.buildDrawingCache();
                                                Bitmap txtBm = qTxt.getDrawingCache();

                                                if (!mmp.checkPermissionForWriteExternalStorage()) {
                                                    mmp.requestPermissionForWriteExternalStorage();
                                                } else {
                                                    Uri uri = getImageUri(Me.this, txtBm);
                                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                                    intent.setType("image/jpeg");
                                                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                                                    intent.putExtra(Intent.EXTRA_TEXT, "A nice quote by " + fnTxt.getText().toString() + " on #InstaQuotes");
                                                    startActivity(Intent.createChooser(intent, "Share on..."));
                                                }
                                        }});


                                        // MARK: - HIDDEN BUTTONS --------------------------------------------------------
                                        Button repButt = finalCell.findViewById(R.id.chReportButt);
                                        repButt.setVisibility(View.INVISIBLE);
                                        Button likeButt = finalCell.findViewById(R.id.chLikeButt);
                                        likeButt.setVisibility(View.INVISIBLE);


                                    }
                                });// end userPointer


                            // QUOTE IS REPORTED!
                            } else {
                                TextView fnTxt =  finalCell.findViewById(R.id.chFullnameTxt);
                                fnTxt.setText("N/A");

                                TextView qTxt =  finalCell.findViewById(R.id.chQuoteTxt);
                                qTxt.setText(pObj.getString(Configs.POSTS_QUOTE));
                                qTxt.setText("THIS QUOTE HAS BEEN REPORTED!");

                                RelativeLayout cellBkg = (RelativeLayout) finalCell.findViewById(R.id.chCellBkg);
                                cellBkg.setBackgroundColor(Color.DKGRAY);

                                final ImageView avImg =  finalCell.findViewById(R.id.chAvatarImg);
                                avImg.setEnabled(false);
                                avImg.setImageResource(R.drawable.logo);
                                Button shareButt =  finalCell.findViewById(R.id.chShareButt);
                                shareButt.setVisibility(View.INVISIBLE);
                                Button repButt =  finalCell.findViewById(R.id.chReportButt);
                                repButt.setVisibility(View.INVISIBLE);
                                Button likeButt =  finalCell.findViewById(R.id.chLikeButt);
                                likeButt.setVisibility(View.INVISIBLE);
                                Button commButt =  finalCell.findViewById(R.id.chCommentButt);
                                commButt.setVisibility(View.INVISIBLE);
                            }

                            return cell;
                        }

                        @Override public int getCount() { return postsArray.size(); }
                        @Override public Object getItem(int position) { return postsArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }

                    // Set ListView adapter
                    quotesListView.setAdapter(new ListAdapter(Me.this, postsArray));


                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), Me.this);
        }}});

    }







    // Method to get URI of the an image
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "image", null);
        return Uri.parse(path);
    }



}//@end
