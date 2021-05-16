package domain.com.newappquotes;

/*----------------------------------

    - InstaQuotes -

    Created by cubycode @2017
    All Rights reserved

-----------------------------------*/

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Comments extends AppCompatActivity {

    /* Views */
    EditText commentTxt;


    /* Variables */
    ParseObject postObj;
    List<ParseObject>commentsArray;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();




        // Init views
        commentTxt = findViewById(R.id.ccCommentEditText);


        // Get objectID from previous .java
        Bundle extras = getIntent().getExtras();
        String objectID = extras.getString("objectID");
        postObj = ParseObject.createWithoutData(Configs.POSTS_CLASS_NAME, objectID);
        try { postObj.fetchIfNeeded().getParseObject(Configs.POSTS_CLASS_NAME);

            // Call query
            queryComments();

        } catch (ParseException e) { e.printStackTrace(); }




        // MARK: - REFRESH BUTTON ------------------------------------
        Button refButt = findViewById(R.id.commRefreshButt);
        refButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
               queryComments();
         }});




        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = findViewById(R.id.commBackButt);
        backButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              finish();
         }});





        // MARK: - SEND A COMMENT -------------------------------------------------------
        Button sendCommButt = findViewById(R.id.comSendCommButt);
        sendCommButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              Configs.showPD("Please wait...", Comments.this);

              ParseObject comObj = new ParseObject(Configs.COMMENTS_CLASS_NAME);

              // Save a userPointer and postPointer
              comObj.put(Configs.COMMENTS_USER_POINTER, ParseUser.getCurrentUser());
              comObj.put(Configs.COMMENTS_POST_POINTER, postObj);
              comObj.put(Configs.COMMENTS_COMMENT, commentTxt.getText().toString());
              comObj.saveInBackground(new SaveCallback() {
                  @Override
                  public void done(ParseException e) {
                      if (e == null) {
                          Configs.hidePD();
                          dismissKeyboard();

                          // Get userPointer
                          postObj.getParseObject(Configs.COMMENTS_USER_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                              public void done(final ParseObject userOfThisPost, ParseException e) {
                                  // Send push notification
                                  final String pushMessage = ParseUser.getCurrentUser().getString(Configs.USER_FULLNAME) +
                                          " commented your quote: " + postObj.getString(Configs.POSTS_QUOTE);

                                  HashMap<String, Object> params = new HashMap<String, Object>();
                                  params.put("someKey", userOfThisPost.getObjectId());
                                  params.put("data", pushMessage);

                                  ParseCloud.callFunctionInBackground("pushAndroid", params, new FunctionCallback<String>() {
                                      @Override
                                      public void done(String object, ParseException e) {
                                          if (e == null) {
                                              Log.i("log-", "PUSH SENT TO: " + userOfThisPost.getString(Configs.USER_USERNAME)
                                                      + "\nMESSAGE: " + pushMessage);

                                          // Error in Cloud Code
                                          } else {
                                              Configs.hidePD();
                                              Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                  }}});


                                  // Save Activity
                                  ParseObject actObj = new ParseObject(Configs.ACTIVITY_CLASS_NAME);
                                  actObj.put(Configs.ACTIVITY_CURRENT_USER, userOfThisPost);
                                  actObj.put(Configs.ACTIVITY_OTHER_USER, ParseUser.getCurrentUser());
                                  actObj.put(Configs.ACTIVITY_TEXT, ParseUser.getCurrentUser().getString(Configs.USER_FULLNAME) +
                                          " commented your quote: " + postObj.getString(Configs.POSTS_QUOTE));
                                  actObj.saveInBackground(new SaveCallback() {
                                      @Override
                                      public void done(ParseException e) {
                                          if (e == null) {

                                              // Recall query
                                              queryComments();

                                          // error
                                          } else {
                                              Configs.hidePD();
                                              Configs.simpleAlert(e.getMessage(), Comments.this);
                                  }}});


                              }}); // end userPointer


                      // error on saving
                      } else {
                          Configs.hidePD();
                          Configs.simpleAlert(e.getMessage(), Comments.this);
              }}});

          }});


    }// end onCreate()






    // MARK: - QUERY COMMENTS --------------------------------------------------
    void queryComments() {
        Configs.showPD("Please wait...", Comments.this);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.COMMENTS_CLASS_NAME);
        query.whereEqualTo(Configs.COMMENTS_POST_POINTER, postObj);
        query.orderByDescending(Configs.POSTS_CREATED_AT);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    commentsArray = objects;
                    Configs.hidePD();

                    // CUSTOM LIST ADAPTER
                    class ListAdapter extends BaseAdapter {
                        private Context context;
                        public ListAdapter(Context context, List<ParseObject> objects) {
                            super();
                            this.context = context;
                        }


                        // CONFIGURE CELL
                        @Override
                        public View getView(int position, View cell, ViewGroup parent) {
                            if (cell == null) {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                assert inflater != null;
                                cell = inflater.inflate(R.layout.cell_comment, null);
                            }
                            final View finalCell = cell;

                            // Get Parse object
                            final ParseObject comObj = commentsArray.get(position);

                            // Get userPointer
                            comObj.getParseObject(Configs.COMMENTS_USER_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(ParseObject userPointer, ParseException e) {

                                    // Get fullname
                                    TextView fnTxt =  finalCell.findViewById(R.id.ccFullnameTxt);
                                    fnTxt.setText(userPointer.getString(Configs.USER_FULLNAME));

                                    // Get Avatar Image
                                    final ImageView anImage =  finalCell.findViewById(R.id.ccAvatarImg);
                                    ParseFile fileObject = userPointer.getParseFile(Configs.USER_AVATAR);
                                    if (fileObject != null) {
                                    fileObject.getDataInBackground(new GetDataCallback() {
                                        public void done(byte[] data, ParseException error) {
                                            if (error == null) {
                                                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                if (bmp != null) {
                                                    anImage.setImageBitmap(bmp);
                                    }}}});}


                                    // Get date
                                    TextView dateTxt =  finalCell.findViewById(R.id.ccDateTxt);
                                    Date aDate = comObj.getCreatedAt();
                                    SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy, hh:mm a");
                                    dateTxt.setText(df.format(aDate));

                                    // Get comment
                                    TextView commTxt =  finalCell.findViewById(R.id.ccCommentTxt);
                                    commTxt.setText(comObj.getString(Configs.COMMENTS_COMMENT));


                                }});// end userPointer


                            return cell;
                        }

                        @Override public int getCount() { return commentsArray.size(); }
                        @Override public Object getItem(int position) { return commentsArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }


                    // Init ListView and set its adapter
                    ListView aList =  findViewById(R.id.commCommentsListView);
                    aList.setAdapter(new ListAdapter(Comments.this, commentsArray));
                    aList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            // Get Parse object
                            ParseObject comObj = commentsArray.get(position);
                            // Get userPointer
                            comObj.getParseObject(Configs.COMMENTS_USER_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(ParseObject userPointer, ParseException e) {

                                    Intent i = new Intent(Comments.this, OtherUserProfile.class);
                                    Bundle extras = new Bundle();
                                    extras.putString("userID", userPointer.getObjectId());
                                    i.putExtras(extras);
                                    startActivity(i);
                            }});// end userPointer
                    }});

                // Error in query
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), Comments.this);
        }}});
    }





    // MARK: - DISMISS KEYBOARD
    public void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(commentTxt.getWindowToken(), 0);
        commentTxt.setText("");
    }

}// @end
