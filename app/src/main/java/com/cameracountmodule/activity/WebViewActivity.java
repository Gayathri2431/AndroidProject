package com.cameracountmodule.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cameracountmodule.R;
import com.cameracountmodule.Utils.Global;
import com.cameracountmodule.manager.ShareManager;

import java.io.ByteArrayInputStream;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class WebViewActivity extends Activity {
	
	private WebView webView;

	public static String EXTRA_URL = "extra_url";
	public static byte[] byteArray;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_webview);
		
		setTitle("Login");

		final String url = this.getIntent().getStringExtra(EXTRA_URL);
		if (null == url) {
			Log.e("Twitter", "URL cannot be null");
			finish();
		}

		webView = (WebView) findViewById(R.id.webView);
		webView.setWebViewClient(new MyWebViewClient());
		webView.loadUrl(url);
	}


	class MyWebViewClient extends WebViewClient {
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			if (url.contains(getResources().getString(R.string.twitter_callback))) {
				try{
					Uri uri = Uri.parse(url);
					String verifier = uri.getQueryParameter(getString(R.string.twitter_oauth_verifier));
					ConfigurationBuilder builder = new ConfigurationBuilder();
					builder.setOAuthConsumerKey("Xi8O0pWHscIqc9SpyIl8JtlSj");
					builder.setOAuthConsumerSecret("bdRD10aVuHTcOG9oLNn1wiBdwjEzM0Bq1Rdo4TEeLHjlYXdJWR");
					AccessToken accessToken = ShareManager.twitter.getOAuthAccessToken(ShareManager.requestToken, verifier);

					AccessToken accessTokenNew = new AccessToken(accessToken.getToken(), accessToken.getTokenSecret());
					Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessTokenNew);
                    Global.settingsManager.setTwitterToken(accessToken.getToken());
                    Global.settingsManager.setTwitterSecret(accessToken.getTokenSecret());
					// Update status
					StatusUpdate statusUpdate = new StatusUpdate("");
					ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
					statusUpdate.setMedia("test.jpg", bis);
					twitter4j.Status response = twitter.updateStatus(statusUpdate);
				} catch (Exception e){
					e.printStackTrace();
				}
				finish();
				return true;
			}
			return false;
		}
	}

}
