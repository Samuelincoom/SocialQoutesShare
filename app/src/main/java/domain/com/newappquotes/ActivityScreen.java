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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ActivityScreen extends AppCompatActivity {


    /* Variables */
    List<ParseObject>activityArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();



        // Call query
        queryActivity();




        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = findViewById(R.id.actBackButt);
        backButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              finish();
         }});



    }// end onCreate()






    // MARK: - QUERY ACTIVITY ------------------------------------------------------
    void queryActivity() {
        Configs.showPD("Please wait...", ActivityScreen.this);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.ACTIVITY_CLASS_NAME);
        query.whereEqualTo(Configs.ACTIVITY_CURRENT_USER, ParseUser.getCurrentUser());
        query.orderByDescending(Configs.ACTIVITY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    activityArray = objects;
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
                                cell = inflater.inflate(R.layout.cell_activity, null);
                            }
                            final View finalCell = cell;


                            // Get Parse object
                            final ParseObject actObj = activityArray.get(position);

                            // Get userPointer
                            actObj.getParseObject(Configs.ACTIVITY_OTHER_USER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(ParseObject userPointer, ParseException e) {

                                    // Get text
                                    TextView actTxt =  finalCell.findViewById(R.id.cactTextTxt);
                                    actTxt.setText(actObj.getString(Configs.ACTIVITY_TEXT));

                                    // Get date
                                    TextView dateTxt =  finalCell.findViewById(R.id.cactDateTxt);
                                    Date aDate = actObj.getCreatedAt();
                                    SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy | hh:mm a");
                                    dateTxt.setText(df.format(aDate));

                                    // Get Avatar
                                    final ImageView anImage =  finalCell.findViewById(R.id.oupAvatarImg);
                                    ParseFile fileObject = userPointer.getParseFile(Configs.USER_AVATAR);
                                    if (fileObject != null) {
                                    fileObject.getDataInBackground(new GetDataCallback() {
                                        public void done(byte[] data, ParseException error) {
                                            if (error == null) {
                                                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                if (bmp != null) {
                                                    anImage.setImageBitmap(bmp);
                                    }}}});}


                                }});// end userPointer


                            return cell;
                        }

                        @Override public int getCount() { return activityArray.size(); }
                        @Override public Object getItem(int position) { return activityArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }

                    // Init ListView and set its adapter
                    ListView aList =  findViewById(R.id.actListView);
                    aList.setAdapter(new ListAdapter(ActivityScreen.this, activityArray));
                    aList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                            ParseObject actObj = activityArray.get(position);

                            // Get userPointer
                            actObj.getParseObject(Configs.ACTIVITY_OTHER_USER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(ParseObject userPointer, ParseException e) {
                                    Intent i = new Intent(ActivityScreen.this, OtherUserProfile.class);
                                    Bundle extras = new Bundle();
                                    extras.putString("userID", userPointer.getObjectId());
                                    i.putExtras(extras);
                                    startActivity(i);

                            }});// end userPointer
                    }});


                // Error in query
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), ActivityScreen.this);
        }}});

    }



}// @end
