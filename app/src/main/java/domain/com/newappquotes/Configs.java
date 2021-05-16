package domain.com.newappquotes;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseUser;


public class Configs extends Application {



    // IMPORTANT: Reaplce the red strings below with your own Application ID and Client Key of your app on back4app.com
    public static String PARSE_APP_KEY = "WbU0KTBf1e54LZCLB7HdWXjQXsMy8tmIKWI6iDSQ";
    public static String PARSE_CLIENT_KEY = "AhF2ynQBvxmtyp9NPA3YHtNSnmGCIPJoufGtievy";
    //-----------------------------------------------------------------------------



    // FOR THE ABOUT CONTROLLER -> Replace the strings below with your social links, your wesbite URL and your admin email
    public static String WEBSITE_URL = "https://github.com/Stride-Global-PLT/Quoteshare-Android";
    public static String FACEBOOK_URL = "https://github.com/Stride-Global-PLT/Quoteshare-Android";
    public static String TWITTER_URL = "https://github.com/Stride-Global-PLT/Quoteshare-Android";
    public static String ADMIN_EMAIL_ADDRESS = "absoluteencore@gmail.com";


    // CUSTOM FONT - (this font is included into app/src/main/assets/font folder)
    public static Typeface bebasFont;



    // ARRAY OF COLORS FOR POSTS (You can edit the HEX values as you wish)
    public static String [] colorsArray = {
            "#c69c6d",
            "#ed5564",
            "#fa6e52",
            "#ffcf55",
            "#a0d468",
            "#48cfae",
            "#4fc0e8",
            "#5d9bec",
            "#ac92ed",
            "#967bdc",
            "#ec88c0",
            "#da4553",
            "#ccd0d9",
            "#434a54",
    };








    /****** DO NOT EDIT THE CODE BELOW *****/

    public static String USER_CLASS_NAME = "_User";
    public static String USER_USERNAME = "username";
    public static String USER_AVATAR = "avatar";
    public static String USER_EMAIL = "email";
    public static String USER_FULLNAME = "fullname";
    public static String USER_IS_REPORTED = "isReported";

    public static String  POSTS_CLASS_NAME = "Posts";
    public static String  POSTS_QUOTE = "quote";
    public static String  POSTS_COLOR = "color";
    public static String  POSTS_USER_POINTER = "userPointer";
    public static String  POSTS_LIKED_BY = "likedBy";  // Array
    public static String  POSTS_KEYWORDS = "keywords";  // Array
    public static String  POSTS_IS_REPORTED = "isReported";
    public static String  POSTS_REPORT_MESSAGE = "reportMessage";
    public static String  POSTS_CREATED_AT = "createdAt";

    public static String  ACTIVITY_CLASS_NAME = "Activity";
    public static String  ACTIVITY_CURRENT_USER = "currentUser";
    public static String  ACTIVITY_OTHER_USER = "otherUser";
    public static String  ACTIVITY_TEXT = "text";
    public static String  ACTIVITY_CREATED_AT = "createdAt";

    public static String  COMMENTS_CLASS_NAME = "Comments";
    public static String  COMMENTS_POST_POINTER = "postPointer";
    public static String  COMMENTS_USER_POINTER = "userPointer";
    public static String  COMMENTS_COMMENT = "comment";



    boolean isParseInitialized = false;

    public void onCreate() {
        super.onCreate();

        // Set font (the font files are into app/src/main/assets/font folder)
        bebasFont = Typeface.createFromAsset(getAssets(),"font/BebasNeue.otf");


        if (!isParseInitialized) {
            Parse.initialize(new Parse.Configuration.Builder(this)
                    .applicationId(String.valueOf(PARSE_APP_KEY))
                    .clientKey(String.valueOf(PARSE_CLIENT_KEY))
                    .server("https://parseapi.back4app.com")
                    .build()
            );
            Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
            ParseUser.enableAutomaticUser();
            isParseInitialized = true;

        }

    }// end onCreate()





    // MARK: - CUSTOM PROGRESS DIALOG -----------
    public static AlertDialog pd;
    public static void showPD(String mess, Context ctx) {
        AlertDialog.Builder db = new AlertDialog.Builder(ctx);
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View dialogView = inflater.inflate(R.layout.pd, null);
        TextView messTxt = dialogView.findViewById(R.id.pdMessTxt);
        messTxt.setText(mess);
        db.setView(dialogView);
        db.setCancelable(true);
        pd = db.create();
        pd.show();
    }
    public static void hidePD(){ pd.dismiss(); }




    // SIMPLE ALERT DIALOG
    public static void simpleAlert(String mess, Context ctx) {
        AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
        alert.setMessage(mess)
            .setTitle(R.string.app_name)
            .setPositiveButton("OK", null)
            .setIcon(R.drawable.logo);
        alert.create().show();
    }




    // MARK: - SCALE BITMAP TO MAX SIZE
    public static Bitmap scaleBitmapToMaxSize(int maxSize, Bitmap bm) {
        int outWidth;
        int outHeight;
        int inWidth = bm.getWidth();
        int inHeight = bm.getHeight();
        if(inWidth > inHeight){
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }
        return Bitmap.createScaledBitmap(bm, outWidth, outHeight, false);
    }



}//@end
