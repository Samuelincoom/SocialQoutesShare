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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Home extends AppCompatActivity {

    /* Views */
    EditText searchTxt;
    ListView quotesListView;

    /* Variables */
    List<ParseObject>postsArray;
    MarshMallowPermission mmp = new MarshMallowPermission(this);




    // ON START() ------------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();

        // CHECK IF YOU'RE LOGGED IN
        if (ParseUser.getCurrentUser().getUsername() != null) {

            // Register GCM Sender ID in Installation class
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();


            // IMPORTANT: REPLACE "478517440140" WITH YOUR OWN GCM Sender ID
            installation.put("GCMSenderId", "53382137202");
            // ----------------------------------------------------


            installation.put("userID", ParseUser.getCurrentUser().getObjectId());
            installation.put("username", ParseUser.getCurrentUser().getUsername());
            installation.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Log.i("log-", "REGISTERED FOR PUSH NOTIFICATIONS!");
            }});

        // Go to Login activity
        } else { startActivity(new Intent(Home.this, Login.class)); }


        // Call query
        if (ParseUser.getCurrentUser().getUsername() != null) { queryPosts(""); }


        // Request Storage permission
        if(!mmp.checkPermissionForReadExternalStorage()) {
            mmp.requestPermissionForReadExternalStorage();
        }

    }






    // ON CREATE() -----------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();



        // Request permission to write storage
        if (!mmp.checkPermissionForWriteExternalStorage()) {
            mmp.requestPermissionForWriteExternalStorage();
        }


        // Init views
        quotesListView = findViewById(R.id.hQuotesListView);


        // Init TabBar buttons
        Button tab_one = findViewById(R.id.tab_two);
        Button tab_two = findViewById(R.id.tab_three);

        tab_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, Me.class));
            }});

        tab_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, About.class));
            }});





        // MARK: - SEARCH POSTS BY KEYWORDS --------------------------------
        searchTxt = findViewById(R.id.hSearchTxt);
        searchTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    dismissKeyboard();
                    queryPosts(searchTxt.getText().toString());

                    return true;
                }
                return false;
        }});




        // MARK: - REFRESH BUTTON ------------------------------------
        Button refButt = findViewById(R.id.hRefreshButt);
        refButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              if (ParseUser.getCurrentUser().getUsername() != null) {
                  queryPosts("");
                  searchTxt.setText("");
              }
        }});




        // MARK: - POST QUOTE BUTTON ------------------------------------
        Button postButt = findViewById(R.id.hPostButt);
        postButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              startActivity(new Intent(Home.this, AddPost.class));
         }});



        // Init AdMob banner
        AdView mAdView =  findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }//end onCreate()






    // MARK: - QUERY POSTS ------------------------------------------------------------------------
    void queryPosts(String text) {
        Configs.showPD("Please wait...", Home.this);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.POSTS_CLASS_NAME);

        if (!text.matches("")) {
            List<String> keywords = new ArrayList<String>();
            String[] one = text.toLowerCase().split(" ");
            for (String keyw : one) { keywords.add(keyw); }
            query.whereContainedIn(Configs.POSTS_KEYWORDS, keywords);
        }

        query.whereEqualTo(Configs.POSTS_IS_REPORTED, false);
        query.orderByDescending(Configs.POSTS_CREATED_AT);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    postsArray = objects;
                    Configs.hidePD();


                   // CUSTOM LIST ADAPTER
                    class ListAdapter extends BaseAdapter {
                        private Context context;
                        public ListAdapter(Context context, List<ParseObject> objects) {
                            super();
                            this.context = context;
                        }


                        // CONFIGURE CELL ------------------------
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
                                public void done(final ParseObject userPointer, ParseException e) {

                                    // Get Quote text
                                    final TextView qTxt =  finalCell.findViewById(R.id.chQuoteTxt);
                                    qTxt.setText(pObj.getString(Configs.POSTS_QUOTE));
                                    qTxt.setTypeface(Configs.bebasFont);

                                    // Get Quote color
                                    int quoteColor = (int) pObj.getNumber(Configs.POSTS_COLOR);
                                    String colorStr = Configs.colorsArray[quoteColor];
                                    RelativeLayout cellBkg = (RelativeLayout)finalCell.findViewById(R.id.chCellBkg);
                                    cellBkg.setBackgroundColor(Color.parseColor(colorStr));
                                    qTxt.setBackgroundColor(Color.parseColor(colorStr));


                                    // Get Fullname
                                    final TextView fnTxt =  finalCell.findViewById(R.id.chFullnameTxt);
                                    fnTxt.setText(userPointer.getString(Configs.USER_FULLNAME));

                                    // Get Avatar
                                    final ImageView avImg =  finalCell.findViewById(R.id.chAvatarImg);
                                    ParseFile fileObject = (ParseFile)userPointer.get(Configs.USER_AVATAR);
                                    if (fileObject != null) {
                                    fileObject.getDataInBackground(new GetDataCallback() {
                                        public void done(byte[] data, ParseException error) {
                                            if (error == null) { Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                if (bmp != null) {
                                                    avImg.setImageBitmap(bmp);
                                    }}}});}


                                    // MARK: - TAP AVATAR -> SEE OTHER USER PROFILE -----------------------------------
                                    avImg.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent i = new Intent(Home.this, OtherUserProfile.class);
                                            Bundle extras = new Bundle();
                                            extras.putString("userID", userPointer.getObjectId());
                                            i.putExtras(extras);
                                            startActivity(i);
                                    }});


                                    // Init like Button
                                    final Button likeButt = finalCell.findViewById(R.id.chLikeButt);


                                    // Set liked button
                                    List<String> postLikedBy;
                                    ParseUser currUser = ParseUser.getCurrentUser();
                                    if ( pObj.getList(Configs.POSTS_LIKED_BY) != null ) {
                                        postLikedBy = pObj.getList(Configs.POSTS_LIKED_BY);
                                        if ( postLikedBy.contains(currUser.getObjectId()) ) {
                                            likeButt.setBackgroundResource(R.drawable.liked_butt);
                                        } else {
                                            likeButt.setBackgroundResource(R.drawable.like_butt);
                                        }
                                    } else {
                                        likeButt.setBackgroundResource(R.drawable.like_butt);
                                    }




                                    // MARK: - LIKE BUTTON --------------------------------------------------------
                                    likeButt.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            final ParseUser currUser = ParseUser.getCurrentUser();

                                            // Get List of likedBy's
                                            List<String> postLikedBy;
                                            postLikedBy = new ArrayList<String>();
                                            if (pObj.getList(Configs.POSTS_LIKED_BY) != null) {
                                                postLikedBy = pObj.getList(Configs.POSTS_LIKED_BY);
                                                Log.i("log-", "LIKED BY: " + postLikedBy);
                                            }


                                            // UNLIKE POST ----------------------------------------------------------------------
                                            if (postLikedBy.contains(currUser.getObjectId())) {
                                                postLikedBy.remove(currUser.getObjectId());
                                                pObj.put(Configs.POSTS_LIKED_BY, postLikedBy);
                                                Log.i("log-", "\nPOST LIKED BY: " + postLikedBy);

                                                likeButt.setBackgroundResource(R.drawable.like_butt);
                                                // Configs.simpleAlert("You've unliked this quote!", Home.this);



                                            // LIKE POST ---------------------------------------------------------------------------
                                            } else {
                                                postLikedBy.add(currUser.getObjectId());
                                                pObj.put(Configs.POSTS_LIKED_BY, postLikedBy);

                                                likeButt.setBackgroundResource(R.drawable.liked_butt);
                                                Configs.simpleAlert("You liked this quote and saved into your account -> Liked Quotes!", Home.this);


                                                // Send Push Notification!
                                                String pushStr = currUser.getString(Configs.USER_FULLNAME)
                                                        + " liked your quote: " +
                                                        pObj.getString(Configs.POSTS_QUOTE);

                                                HashMap<String, Object> params = new HashMap<String, Object>();
                                                params.put("someKey", userPointer.getObjectId());
                                                params.put("data", pushStr);
                                                ParseCloud.callFunctionInBackground("pushAndroid", params, new FunctionCallback<String>() {
                                                    public void done(String success, ParseException e) {
                                                        if (e == null) {

                                                            Log.i("log-", "PUSH SENT! " + userPointer.getString(Configs.USER_USERNAME));

                                                        // Error in Cloud Code
                                                        } else {
                                                            Configs.hidePD();
                                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                                }}});



                                                // Save Activity
                                                ParseObject aObj = new ParseObject(Configs.ACTIVITY_CLASS_NAME);
                                                aObj.put(Configs.ACTIVITY_CURRENT_USER, userPointer);
                                                aObj.put(Configs.ACTIVITY_OTHER_USER, currUser);
                                                aObj.put(Configs.ACTIVITY_TEXT, currUser.getString(Configs.USER_FULLNAME) +
                                                        " liked your quote: " + pObj.getString(Configs.POSTS_QUOTE));
                                                aObj.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        Log.i("log-", "NEW ACTIVITY SAVED!");
                                                 }});
                                            }



                                            // UPDATE POST ------------------------------------------------------------------------
                                            pObj.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        Configs.hidePD();
                                                    } else {
                                                        Configs.hidePD();
                                                        Configs.simpleAlert(e.getMessage(), Home.this);
                                            }}});


                                    }});// end LikeButt




                                    // MARK: - SHARE QUOTE BUTTON --------------------------------------------------------
                                    Button shareButt = finalCell.findViewById(R.id.chShareButt);
                                    shareButt.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            // Make a bitmap out of the TextView
                                            qTxt.buildDrawingCache();
                                            Bitmap txtBm = qTxt.getDrawingCache();
                                            Bitmap bitmap = Bitmap.createScaledBitmap(txtBm, 1080,1080 , false);

                                            if (!mmp.checkPermissionForWriteExternalStorage()) {
                                                mmp.requestPermissionForWriteExternalStorage();
                                            } else {
                                                Uri uri = getImageUri(Home.this, bitmap);
                                                Intent intent = new Intent(Intent.ACTION_SEND);
                                                intent.setType("image/jpeg");
                                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                                intent.putExtra(Intent.EXTRA_TEXT, "A nice quote by " +
                                                        fnTxt.getText().toString() + " on #InstaQuotes");
                                                startActivity(Intent.createChooser(intent, "Share on..."));
                                            }
                                    }});


                                    // MARK: - COMMENT BUTTON --------------------------------------------------------
                                    Button commButt = finalCell.findViewById(R.id.chCommentButt);
                                    commButt.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                           Intent i = new Intent(Home.this, Comments.class);
                                           Bundle extras = new Bundle();
                                           extras.putString("objectID", pObj.getObjectId());
                                           i.putExtras(extras);
                                           startActivity(i);
                                    }});




                                    // MARK: - REPORT QUOTE BUTTON ------------------------------------
                                    Button repButt = finalCell.findViewById(R.id.chReportButt);
                                    repButt.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View view) {
                                          final ParseObject quoteObj = postsArray.get(position);
                                          Log.i("log-", "POST: " + quoteObj.getString(Configs.POSTS_QUOTE));

                                          AlertDialog.Builder alert = new AlertDialog.Builder(Home.this);
                                         final EditText editTxt = new EditText(Home.this);
                                          editTxt.setHint(" type here...");
                                          alert.setMessage("Tell us briefly why you're reporting this Quote")
                                             .setView(editTxt)
                                             .setTitle(R.string.app_name)
                                             .setIcon(R.drawable.logo)
                                             .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                 @Override
                                                 public void onClick(DialogInterface dialog, int which) {
                                                     String textStr = editTxt.getText().toString();

                                                     quoteObj.put(Configs.POSTS_IS_REPORTED, true);
                                                     quoteObj.put(Configs.POSTS_REPORT_MESSAGE, textStr);
                                                     quoteObj.saveInBackground(new SaveCallback() {
                                                         @Override
                                                         public void done(ParseException e) {
                                                             if (e == null) {
                                                                 Configs.simpleAlert("Thanks for reporting this Quote.\nWe'll check it out within 24h!", Home.this);

                                                                 // Remove selected row
                                                                 postsArray.remove(position);
                                                                 quotesListView.invalidateViews();
                                                                 quotesListView.refreshDrawableState();
                                                     }}});

                                             }})
                                             .setNegativeButton("Cancel", null);
                                          alert.create().show();

                                     }});



                                }});// end userPointer


                            return cell;
                        }

                        @Override
                        public int getCount() { return postsArray.size(); }
                        @Override
                        public Object getItem(int position) { return postsArray.get(position); }
                        @Override
                        public long getItemId(int position) { return position; }
                    }


                    // Init ListView and set its adapter
                    quotesListView =  findViewById(R.id.hQuotesListView);
                    quotesListView.setAdapter(new ListAdapter(Home.this, postsArray));


                // Error in query
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), Home.this);
        }}});

    }





    // Method to get URI of the an image
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "image", null);
        return Uri.parse(path);
    }






    // MARK: - DISMISS KEYBOARD
   public void dismissKeyboard() {
       InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
       imm.hideSoftInputFromWindow(searchTxt.getWindowToken(), 0);
   }


}//@end



