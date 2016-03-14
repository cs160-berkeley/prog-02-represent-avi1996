package edu.berkeley.cs160.represent.represent.apis;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.berkeley.cs160.represent.represent.Representative;
import edu.berkeley.cs160.represent.represent.RepsListActivity;
import edu.berkeley.cs160.represent.represent.utils.NetworkUtils;

/**
 * Created by Avi on 3/8/16.
 */
public class SunlightAPI {

    private static final String API_KEY = "ee53ce5c27bc49f4811b37f74212e4ad";
    private static final String API_KEY_STRING_EXTENSION = "&apikey=" + API_KEY;

    private static final String REPS_URL_ZIP = "https://congress.api.sunlightfoundation.com/legislators/locate?zip=";
    private static final String REPS_URL_LOCATION = "https://congress.api.sunlightfoundation.com/legislators/locate?latitude=";
    private static final String REPS_URL_LONGITUDE_EXTENSION = "&longitude=";
    private static final String TAG = "SunlightAPI";

    private static final String COMMITTEES_URL = "https://congress.api.sunlightfoundation.com/committees?member_ids=";
    private static final String BILLS_URL = "https://congress.api.sunlightfoundation.com/bills/search?sponsor_id=";

    private static final String TYPE_KEY = "title";
    private static final String TYPE_SENATOR = "Sen";
    private static final String TYPE_HOUSE_REP = "Rep";

    private static final String FIRST_NAME_KEY = "first_name";
    private static final String LAST_NAME_KEY = "last_name";
    private static final String CONTACT_KEY = "contact_form";
    private static final String WEBSITE_KEY = "website";
    private static final String TWITTER_KEY = "twitter_id";

    private static final String PARTY_KEY = "party";

    private static final String EOT_KEY = "term_end";
    private static final Object BIOGUIDE_KEY = "bioguide_id";

    private static String zip;

    public static void getRepresentatives(Double latitude, Double longitude, Activity activity, String place) {
        String latitudeParam = latitude.toString();
        String longitudeParam = longitude.toString();
        String url = REPS_URL_LOCATION + latitudeParam + REPS_URL_LONGITUDE_EXTENSION + longitudeParam + API_KEY_STRING_EXTENSION;
        new SunlightTask(activity, RepsListActivity.TYPE_GET_LOCATION, place).execute(url);

    }

    public static void getRepresentatives(String zip, Activity activity) {
        SunlightAPI.zip = zip;
        String url = REPS_URL_ZIP + zip + API_KEY_STRING_EXTENSION;
        new SunlightTask(activity, RepsListActivity.TYPE_SEARCH_LOCATION, null).execute(url);
    }

    private static class SunlightTask extends AsyncTask<String, Void, String> {


        private static final String TAG = "SunlightTask";
        private Activity activity;
        private int REQUEST_TYPE;
        private String place;

        public SunlightTask(Activity activity, int REQUEST_TYPE, String place) {
            this.activity = activity;
            this.REQUEST_TYPE = REQUEST_TYPE;
            this.place = place;
        }

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            try {
                return NetworkUtils.sendGET(url);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            processRepresentativeData(s, activity, REQUEST_TYPE, place);
        }
    }

    private static Representative setRepresentativeData(LinkedTreeMap rep, final boolean last, final Activity activity, final int requestType, final String place) {
        int type = 0, party = 0;
        String name, email, website, twitterHandle, endOfTerm;

        String typeString = rep.get(TYPE_KEY).toString();
        if (typeString.equals(TYPE_SENATOR)) {
            type = Representative.TYPE_SENATOR;
        } else if (typeString.equals(TYPE_HOUSE_REP)) {
            type = Representative.TYPE_HOUSE_REP;
        }

        String partyString = rep.get(PARTY_KEY).toString();
        if (partyString.equals("R")) {
            party = Representative.PARTY_REPUBLICAN;
        } else if (partyString.equals("D")) {
            party = Representative.PARTY_DEMOCRAT;
        } else if (partyString.equals("I")) {
            party = Representative.PARTY_INDEPENDENT;
        }

        name = rep.get(FIRST_NAME_KEY).toString() + " " + rep.get(LAST_NAME_KEY).toString();
        if (rep.get(CONTACT_KEY) == null) {
            email = "";
        } else {
            email = rep.get(CONTACT_KEY).toString();
        }
        if (rep.get(WEBSITE_KEY) == null) {
            website = "";
        } else {
            website = rep.get(WEBSITE_KEY).toString();
        }
        if (rep.get(TWITTER_KEY) == null) {
            twitterHandle = "";
        } else {
            twitterHandle = rep.get(TWITTER_KEY).toString();
        }
        endOfTerm = rep.get(EOT_KEY).toString();

        final Representative toRet = new Representative(type, name, email, website, twitterHandle, party, endOfTerm);
        
        toRet.setBioguideID(rep.get(BIOGUIDE_KEY).toString());

        new CommitteesTask(toRet).execute(toRet.getBioguideID());
        new BillsTask(toRet).execute(toRet.getBioguideID());

        final TwitterApiClient twitterApiClient = Twitter.getApiClient();
        twitterApiClient.getStatusesService().userTimeline(null, toRet.getTwitterHandle(), null, 1L, null, false, false, false, false, new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> result) {
                toRet.setLastTweet(result.data.get(0).getId());
                if (last) {
                    transitionToRepsActivity(requestType, zip, activity, place);
                }
                toRet.setPicUrl(result.data.get(0).user.profileImageUrl);
            }

            @Override
            public void failure(TwitterException e) {
                Log.d(TAG, "LAST TWEET FAILED");
            }
        });


        return toRet;
    }

    private static void transitionToRepsActivity(int type, String zip, Activity activity, String place) {
        Intent i = new Intent(activity, RepsListActivity.class);
        i.putExtra("type", type);
        if (zip != null) {
            i.putExtra("zip", zip);
        } else {
            i.putExtra("place", place);
        }
        activity.startActivity(i);
//        activity.finish();
    }


    private static void processRepresentativeData(String s, Activity activity, int requestType, String place) {
        Gson gson = new Gson();
        HashMap map = gson.fromJson(s, HashMap.class);
        ArrayList reps = (ArrayList) map.get("results");
        Representative.clearRepresentativeHashMap();
        for (int i = 0; i < reps.size(); i++) {
            LinkedTreeMap rep = (LinkedTreeMap) reps.get(i);
            if (i == reps.size() - 1) {
                Representative newRep = setRepresentativeData(rep, true, activity, requestType, place);
            } else {
                Representative newRep = setRepresentativeData(rep, false, activity, requestType, place);
            }
        }
    }

    private static class CommitteesTask extends AsyncTask<String, Void, String> {

        Representative rep;

        public CommitteesTask(Representative rep) {
            this.rep = rep;
        }

        @Override
        protected String doInBackground(String... params) {
            String url = COMMITTEES_URL + params[0] + API_KEY_STRING_EXTENSION;
            try {
                return NetworkUtils.sendGET(url);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Gson gson = new Gson();
            HashMap map = gson.fromJson(s, HashMap.class);
            ArrayList committees = (ArrayList) map.get("results");
            for (Object o: committees) {
                LinkedTreeMap committee = (LinkedTreeMap) o;
                rep.getCommitteesList().add(committee.get("name").toString());
            }
        }
    }

    private static class BillsTask extends AsyncTask<String, Void, String> {

        Representative rep;

        public BillsTask(Representative rep) {
            this.rep = rep;
        }

        @Override
        protected String doInBackground(String... params) {
            String url = BILLS_URL + params[0] + API_KEY_STRING_EXTENSION;
            try {
                return NetworkUtils.sendGET(url);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Gson gson = new Gson();
            HashMap map = gson.fromJson(s, HashMap.class);
            ArrayList committees = (ArrayList) map.get("results");
            for (Object o: committees) {
                LinkedTreeMap committee = (LinkedTreeMap) o;
                Object title = committee.get("short_title");
                if (title == null) {
                    title = committee.get("official_title");
                }
                String date = committee.get("introduced_on").toString();
                rep.getSponsoredBillsList().put(title.toString(), date);
            }
        }
    }





}
