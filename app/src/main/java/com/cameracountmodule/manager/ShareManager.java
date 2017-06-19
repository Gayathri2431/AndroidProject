package com.cameracountmodule.manager;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore.Images;

import com.cameracountmodule.Utils.Global;
import com.cameracountmodule.activity.WebViewActivity;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.cameracountmodule.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class ShareManager {

    private static ShareManager instance = null;
    private ShareDialog shareDialog;
    private Context context;
    private LoginManager manager;
    public CallbackManager callbackManager;

    public static final int WEBVIEW_REQUEST_CODE = 100;
    public static Twitter twitter;
    public static RequestToken requestToken;
    private String consumerKey = "Xi8O0pWHscIqc9SpyIl8JtlSj";
    private String consumerSecret = "bdRD10aVuHTcOG9oLNn1wiBdwjEzM0Bq1Rdo4TEeLHjlYXdJWR";
    private String callbackUrl = "http://www.cognitivemachines.co.in";
    private String oAuthVerifier = "oauth_verifier";

    private ShareManager(Context context) {
        this.context = context;
        FacebookSdk.sdkInitialize(context);
        CallbackManager.Factory.create();
        shareDialog = new ShareDialog((Activity) context);
        callbackManager = CallbackManager.Factory.create();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public static ShareManager getInstance(Context context) {
        instance = new ShareManager(context);
        return instance;
    }

    public static ShareManager oldInstance(Context context){
        if (instance == null) {
            instance = new ShareManager(context);
        }
        return instance;
    }


    public void shareFB(Bitmap bitmap) {
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(bitmap)
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();
        shareDialog.show(content);
    }

    public void shareTwitter(String shareTitle, Bitmap bitmap) {
        final ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(consumerKey);
        builder.setOAuthConsumerSecret(consumerSecret);

        final Configuration configuration = builder.build();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        try {
            if(!Global.settingsManager.getTwitterToken().equals("")){
                final ConfigurationBuilder builderNew = new ConfigurationBuilder();
                builderNew.setOAuthConsumerKey(consumerKey);
                builderNew.setOAuthConsumerSecret(consumerSecret);
                twitter4j.auth.AccessToken accessTokenNew = new twitter4j.auth.AccessToken(Global.settingsManager.getTwitterToken(), Global.settingsManager.getTwitterSecret());
                Twitter twitter = new TwitterFactory(builderNew.build()).getInstance(accessTokenNew);
                StatusUpdate statusUpdate = new StatusUpdate(shareTitle);
                ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
                statusUpdate.setMedia("test.jpg", bis);
                twitter4j.Status response = twitter.updateStatus(statusUpdate);
                Global.showSharedMessage(context, context.getResources().getString(R.string.photo_shared_on_twiter));
            }else{
                final TwitterFactory factory = new TwitterFactory(configuration);
                twitter = factory.getInstance();
                requestToken = twitter.getOAuthRequestToken(callbackUrl);
                final Intent intent = new Intent((Activity)context, WebViewActivity.class);
                WebViewActivity.byteArray = byteArray;
                intent.putExtra(WebViewActivity.EXTRA_URL, requestToken.getAuthenticationURL());
                //intent.putExtra("photo",byteArray);
                Activity activityObj = (Activity)context;
                activityObj.startActivityForResult(intent, WEBVIEW_REQUEST_CODE);
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    public void shareWhatsApp(String shareTitle, Bitmap bitmap) {
        if (!GooglePlayApp.isInstalledApp(context, "com.whatsapp"))
            return;

        Intent waIntent = new Intent(Intent.ACTION_SEND);
        waIntent.setType("image/*");

        String bitmapPath = Images.Media.insertImage(context.getContentResolver(), bitmap, shareTitle, null);
        Uri bitmapUri = Uri.parse(bitmapPath);

        waIntent.putExtra(Intent.EXTRA_TEXT, shareTitle);
        waIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        waIntent.setPackage("com.whatsapp");

        context.startActivity(waIntent);
    }

    public CallbackManager getCM(){
        return callbackManager;
    }
}
