package edu.berkeley.cs160.represent.represent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class RepsListActivity extends RepresentActivity implements View.OnClickListener {

    public static final int TYPE_SEARCH_LOCATION = 0;
    public static final int TYPE_GET_LOCATION = 1;
    private static final String TAG = "RepsListActivity";
    private static Representative senatorOne;
    private static Representative senatorTwo;
    private static Representative repOne;
    private int type;
    private String zip;
    private ActionBar mActionBar;
    private RelativeLayout mSenatorOne;
    private RelativeLayout mSenatorTwo;
    private RepArrayAdapter mRepArrayAdapter;
    private RepArrayAdapter mSenatorArrayAdapter;
    private List<Node> nodes;

    private HashMap<String, TweetView> cachedTweetViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reps_list);

        type = getIntent().getIntExtra("type", 0);

        zip = getIntent().getStringExtra("zip");

        mActionBar = getSupportActionBar();

        if (type == TYPE_GET_LOCATION) {
            getSupportActionBar().setTitle("    " + getIntent().getStringExtra("place"));
            getSupportActionBar().setIcon(ContextCompat.getDrawable(this, R.drawable.ic_my_location_white_24dp));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        } else if (type == TYPE_SEARCH_LOCATION){
            getSupportActionBar().setTitle("    " + zip);
            getSupportActionBar().setIcon(ContextCompat.getDrawable(this, R.drawable.ic_search_white_24dp));
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        cachedTweetViews = new HashMap<>();

        mRepArrayAdapter = new RepArrayAdapter(this, R.layout.collapsed_rep_item);
        mSenatorArrayAdapter = new RepArrayAdapter(this, R.layout.collapsed_rep_item);

//        mRepArrayAdapter.addAll(Representative.getRepresentatives());
        mSenatorArrayAdapter.add(new Representative());
        mSenatorArrayAdapter.addAll(Representative.getSenators());
        mSenatorArrayAdapter.add(new Representative());
        mSenatorArrayAdapter.addAll(Representative.getRepresentatives());

        final ListView senators = (ListView) findViewById(R.id.senators_section);
        senators.setVisibility(View.VISIBLE);
        senators.setAdapter(mSenatorArrayAdapter);

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                nodes = getConnectedNodesResult.getNodes();
                Log.d(TAG, "found nodes");
                for (Node n : nodes) {
                    Log.d(TAG, n.getDisplayName());
                }
                ArrayList<String> reps = new ArrayList<>();
                for (Representative r: Representative.getSenators()) {
                    reps.add(r.getPartyText() + " Senator " + r.getName());
                }
                for (Representative r: Representative.getRepresentatives()) {
                    reps.add(r.getPartyText() + " Representative " + r.getName());
                }
                sendMessage(Representative.getCounty(), reps.toArray());
            }
        });
    }

    private void sendMessage(String county, Object[] reps) {

        if (mGoogleApiClient.isConnected()) {
            for (Node node : nodes) {
//            Wearable.MessageApi.sendMessage(
//                    mGoogleApiClient, node.getId(), path, text.getBytes());
                String WEARABLE_DATA_PATH = "/wearable_data";

                // Create a DataMap object and send it to the data layer
                DataMap dataMap = new DataMap();
                dataMap.putString("county_name", county);
                if (Representative.getObama() != null && Representative.getMitt() != null) {
                    dataMap.putString("barack", Representative.getObama().toString());
                    dataMap.putString("mitt", Representative.getMitt().toString());
                } else {
                    dataMap.putString("barack", "");
                    dataMap.putString("mitt", "");
                }

                dataMap.putString("rep_count", ((Integer) reps.length).toString());
                for (Integer i = 0; i < reps.length; i++) {
                    dataMap.putString(i.toString(), ((String) reps[i]));
                }
                //Requires a new thread to avoid blocking the UI
                new SendToDataLayerThread(WEARABLE_DATA_PATH, dataMap).start();
            }
        }


    }

    private class SendToDataLayerThread extends Thread {
        String path;
        DataMap dataMap;

        // Constructor for sending data objects to the data layer
        SendToDataLayerThread(String p, DataMap data) {
            path = p;
            dataMap = data;
        }

        public void run() {
            // Construct a DataRequest and send over the data layer
            PutDataMapRequest putDMR = PutDataMapRequest.create(path);
            putDMR.getDataMap().putAll(dataMap);
            PutDataRequest request = putDMR.asPutDataRequest();
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
            if (result.getStatus().isSuccess()) {
                Log.v(TAG, "DataMap: " + dataMap + " sent successfully to data layer ");
            }
            else {
                // Log an error
                Log.v(TAG, "ERROR: failed to send DataMap to data layer");
            }
        }
    }

//    private void setStaticReps() {
//        senatorOne = new Representative(Representative.TYPE_SENATOR, "Johnny Isakson", "info@johnnyisakson.com", "www.johnnyisakson.com",
//                "@SenatorIsakson", "On #PresidentsDay we honor those who have served in our nation's highest office.", "https://upload.wikimedia.org/wikipedia/commons/e/ef/Johnny_Isakson_official_Senate_photo.jpg", Representative.PARTY_REPUBLICAN, "", null, null);
//
//        ArrayList<String> senTwoCommittees = new ArrayList<>();
//        senTwoCommittees.add("Special Committee on Aging");
//        senTwoCommittees.add("Agriculture, Nutrition, and Forestry");
//        senTwoCommittees.add("Budget");
//        senTwoCommittees.add("Foreign Relations");
//        senTwoCommittees.add("Judiciary");
//
//        ArrayList<String> senTwoBills = new ArrayList<>();
//        senTwoBills.add("Trade Act of 2015 (5/22/15)");
//        senTwoBills.add("Trade Promotion Authority (6/24/15");
//        senTwoBills.add("Iran nuclear deal (5/7/2015)");
//
//        senatorTwo = new Representative(Representative.TYPE_SENATOR, "David Perdue", "www.perdue.senate.gov/connect", "www.perdue.senate.gov",
//                "@sendavidperdue", "A member of my staff made a new friend after a meeting in Macon County last week", "https://upload.wikimedia.org/wikipedia/commons/5/53/David_Perdue_official_Senate_photo.jpg", Representative.PARTY_REPUBLICAN, "Jan 3, 2021",
//                senTwoCommittees, senTwoBills);
//
//        repOne = new Representative(Representative.TYPE_HOUSE_REP, "Tom Price", "www.tomprice.house.gov/contact-me", "www.tomprice.house.gov",
//                "@RepTomPrice", "Real debt reduction will require WH cooperation.", "https://upload.wikimedia.org/wikipedia/commons/0/05/Tom_Price.jpg", Representative.PARTY_REPUBLICAN, "", null, null);
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.rep_more_info:
//                Intent i = new Intent(this, RepresentativeActivity.class);
//                i.putExtra("rep", senatorTwo.getName());
//                startActivity(i);
//                finish();
//                break;

        }
    }

    private class RepArrayAdapter extends ArrayAdapter<Representative> {

        public RepArrayAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.collapsed_rep_item, parent, false);
            }

            Representative rep = getItem(position);

            if (rep.getType() == Representative.TYPE_NOT_REAL) {
                row.findViewById(R.id.rep_info).setVisibility(View.GONE);
                if (rep.getFakeId() == 0) {
                    row.findViewById(R.id.divider_text_section).setVisibility(View.VISIBLE);
                    ((TextView) row.findViewById(R.id.senators_text)).setText("SENATORS");
                } else if (rep.getFakeId() == 1) {
                    row.findViewById(R.id.divider_text_section).setVisibility(View.VISIBLE);
                    ((TextView) row.findViewById(R.id.senators_text)).setText("REPRESENTATIVES");
                }
            } else {
                row.findViewById(R.id.rep_info).setVisibility(View.VISIBLE);
                row.findViewById(R.id.divider_text_section).setVisibility(View.GONE);
                Log.d(TAG, rep.getName());
                insertRepIntoField((RelativeLayout) row, rep);
            }

            return row;

        }

        private void insertRepIntoField(final RelativeLayout row, final Representative rep) {

            final RelativeLayout view = (RelativeLayout) row.findViewById(R.id.rep_info);
            // TODO: Use a more specific parent
            final RelativeLayout parentView = (RelativeLayout) view.findViewById(R.id.tweet_section);
//            Log.d(TAG, rep.getName() + " " + parentView.toString());
//             TODO: Base this Tweet ID on some data from elsewhere in your app
            if (cachedTweetViews.containsKey(rep.getBioguideID())) {
                if (cachedTweetViews.get(rep.getBioguideID()).getParent() == null) {
                    parentView.addView(cachedTweetViews.get(rep.getBioguideID()));
                }
                ((TextView) view.findViewById(R.id.collapsed_rep_item_name)).setText(rep.getName());
                ((TextView) view.findViewById(R.id.collapsed_rep_item_name)).bringToFront();
                ((TextView) view.findViewById(R.id.rep_email)).setText(rep.getEmail());
                ((TextView) view.findViewById(R.id.rep_email)).bringToFront();
                ((TextView) view.findViewById(R.id.rep_website)).setText(rep.getWebsite());
                ((TextView) view.findViewById(R.id.rep_website)).bringToFront();

                if (rep.getParty() == Representative.PARTY_INDEPENDENT) {
                    row.findViewById(R.id.rep_info).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.reps_shape_independent));
                } else if (rep.getParty() == Representative.PARTY_REPUBLICAN) {
                    row.findViewById(R.id.rep_info).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.reps_shape_republican));
                } else if (rep.getParty() == Representative.PARTY_DEMOCRAT) {
                    row.findViewById(R.id.rep_info).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.reps_shape_democrat));
                }
            } else {
                TweetUtils.loadTweet(rep.getLastTweet(), new Callback<Tweet>() {
                    @Override
                    public void success(Result<Tweet> result) {
                        TweetView tweetView = new TweetView(getContext(), result.data);
                        parentView.addView(tweetView);
                        ((TextView) view.findViewById(R.id.collapsed_rep_item_name)).setText(rep.getName());
                        ((TextView) view.findViewById(R.id.collapsed_rep_item_name)).bringToFront();
                        ((TextView) view.findViewById(R.id.rep_email)).setText(rep.getEmail());
                        ((TextView) view.findViewById(R.id.rep_email)).bringToFront();
                        ((TextView) view.findViewById(R.id.rep_website)).setText(rep.getWebsite());
                        ((TextView) view.findViewById(R.id.rep_website)).bringToFront();
                        ((ImageView) view.findViewById(R.id.more_rep_info)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(getContext(), RepresentativeActivity.class);
                                i.putExtra("rep", rep.getName());
                                startActivity(i);
                            }
                        });

                        if (rep.getParty() == Representative.PARTY_INDEPENDENT) {
                            row.findViewById(R.id.rep_info).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.reps_shape_independent));
                        } else if (rep.getParty() == Representative.PARTY_REPUBLICAN) {
                            row.findViewById(R.id.rep_info).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.reps_shape_republican));
                        } else if (rep.getParty() == Representative.PARTY_DEMOCRAT) {
                            row.findViewById(R.id.rep_info).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.reps_shape_democrat));
                        }
                        cachedTweetViews.put(rep.getBioguideID(), tweetView);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        Log.d("TwitterKit", "Load Tweet failure", exception);
                    }
                });
            }

            }
        }

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }
}
