package edu.berkeley.cs160.represent.represent;

import android.util.Log;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.AppSession;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Avi on 2/28/16.
 */
public class RepresentApplication extends android.app.Application {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "pqMAZLlUUM2pWhJn9pi0DCGeM";
    private static final String TWITTER_SECRET = "XCGP8R9PYbvH1FcJXPAYBAwpsNHu7SLO2P8PsekKD4hsoqVlvk";
    private static final String TAG = "RepresentApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        TwitterCore.getInstance().logInGuest(new Callback<AppSession>() {
            @Override
            public void success(Result<AppSession> appSessionResult) {
                Log.d(TAG, "TWITTER SESSION IS SUCCESSFUL");
            }
            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
            }
        });

//        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//                        .setDefaultFontPath("fonts/")
//                        .setFontAttrId(R.attr.fontPath)
//                        .build()
//        );
    }

}
