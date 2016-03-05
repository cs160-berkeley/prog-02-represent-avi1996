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
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Collection;
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
    private boolean wearConnected;
    private List<Node> nodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reps_list);

        type = getIntent().getIntExtra("type", 0);

        zip = getIntent().getStringExtra("zip");

        mActionBar = getSupportActionBar();

        if (type == TYPE_GET_LOCATION) {
            getSupportActionBar().setTitle("    Current Location");
            getSupportActionBar().setIcon(ContextCompat.getDrawable(this, R.drawable.ic_my_location_white_24dp));
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            zip = "30005";

        } else if (type == TYPE_SEARCH_LOCATION){
            getSupportActionBar().setTitle("    " + zip);
            getSupportActionBar().setIcon(ContextCompat.getDrawable(this, R.drawable.ic_search_white_24dp));
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setStaticReps();

        mSenatorOne = (RelativeLayout) findViewById(R.id.senator_one);
        mSenatorTwo = (RelativeLayout) findViewById(R.id.senator_two);

        insertRepIntoField(mSenatorOne, senatorOne);
        insertRepIntoField(mSenatorTwo, senatorTwo);

        mRepArrayAdapter = new RepArrayAdapter(this, R.layout.collapsed_rep_item_row);

        mRepArrayAdapter.add(repOne);

        final ListView reps = (ListView) findViewById(R.id.reps_section);
        reps.setVisibility(View.VISIBLE);
        reps.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        reps.setAdapter(mRepArrayAdapter);

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);
        wearConnected = true;

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                nodes = getConnectedNodesResult.getNodes();
                Log.d(TAG, "found nodes");
                for (Node n : nodes) {
                    Log.d(TAG, n.getDisplayName());
                }
                sendMessage("/location", zip);
            }
        });
    }

    private void sendMessage(final String path, final String text ) {
        for (Node node : nodes) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node.getId(), path, text.getBytes());
        }

    }



    private void insertRepIntoField(RelativeLayout view, Representative rep) {
        if (rep.getParty() == Representative.PARTY_INDEPENDENT) {
            view.findViewById(R.id.rep_item_background).setBackground(ContextCompat.getDrawable(this, R.drawable.reps_shape_independent));
        } else if (rep.getParty() == Representative.PARTY_REPUBLICAN) {
            view.findViewById(R.id.rep_item_background).setBackground(ContextCompat.getDrawable(this, R.drawable.reps_shape_republican));
        } else if (rep.getParty() == Representative.PARTY_DEMOCRAT) {
            view.findViewById(R.id.rep_item_background).setBackground(ContextCompat.getDrawable(this, R.drawable.reps_shape_democrat));
        }

        ImageUtils.loadImageFromUrl(this, rep.getPicUrl(), ((ImageView) view.findViewById(R.id.rep_photo)));
        ((TextView) view.findViewById(R.id.collapsed_rep_item_name)).setText(rep.getName());
        if (rep.getName().equals("David Perdue"))
            view.findViewById(R.id.rep_more_info).setOnClickListener(this);
        ((TextView) view.findViewById(R.id.rep_email)).setText(rep.getEmail());
        ((TextView) view.findViewById(R.id.rep_website)).setText(rep.getWebsite());
        ((TextView) view.findViewById(R.id.rep_twitter_handle)).setText(rep.getTwitterHandle());
        ((TextView) view.findViewById(R.id.rep_last_tweet)).setText(rep.getLastTweet());


    }

    private void setStaticReps() {
        senatorOne = new Representative(Representative.TYPE_SENATOR, "Johnny Isakson", "info@johnnyisakson.com", "www.johnnyisakson.com",
                "@SenatorIsakson", "On #PresidentsDay we honor those who have served in our nation's highest office.", "https://upload.wikimedia.org/wikipedia/commons/e/ef/Johnny_Isakson_official_Senate_photo.jpg", Representative.PARTY_REPUBLICAN, "", null, null);

        ArrayList<String> senTwoCommittees = new ArrayList<>();
        senTwoCommittees.add("Special Committee on Aging");
        senTwoCommittees.add("Agriculture, Nutrition, and Forestry");
        senTwoCommittees.add("Budget");
        senTwoCommittees.add("Foreign Relations");
        senTwoCommittees.add("Judiciary");

        ArrayList<String> senTwoBills = new ArrayList<>();
        senTwoBills.add("Trade Act of 2015 (5/22/15)");
        senTwoBills.add("Trade Promotion Authority (6/24/15");
        senTwoBills.add("Iran nuclear deal (5/7/2015)");

        senatorTwo = new Representative(Representative.TYPE_SENATOR, "David Perdue", "www.perdue.senate.gov/connect", "www.perdue.senate.gov",
                "@sendavidperdue", "A member of my staff made a new friend after a meeting in Macon County last week", "https://upload.wikimedia.org/wikipedia/commons/5/53/David_Perdue_official_Senate_photo.jpg", Representative.PARTY_REPUBLICAN, "Jan 3, 2021",
                senTwoCommittees, senTwoBills);

        repOne = new Representative(Representative.TYPE_HOUSE_REP, "Tom Price", "www.tomprice.house.gov/contact-me", "www.tomprice.house.gov",
                "@RepTomPrice", "Real debt reduction will require WH cooperation.", "https://upload.wikimedia.org/wikipedia/commons/0/05/Tom_Price.jpg", Representative.PARTY_REPUBLICAN, "", null, null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rep_more_info:
                Intent i = new Intent(this, RepresentativeActivity.class);
                i.putExtra("rep", senatorTwo.getName());
                startActivity(i);
                finish();
                break;

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
                row = inflater.inflate(R.layout.collapsed_rep_item_row, parent, false);
            }

            Representative currentRep = null;
            Representative nextRep = null;

            if (position % 2 == 0) {
                currentRep = getItem(position);
                if (position + 1 < getCount())
                        nextRep = getItem(position + 1);
            } else {
                return row;
            }

            insertRepIntoField(((RelativeLayout) row.findViewById(R.id.rep_one)), currentRep);
            if (nextRep != null) {
                insertRepIntoField(((RelativeLayout) row.findViewById(R.id.rep_two)), nextRep);
            } else {
                row.findViewById(R.id.rep_two).setVisibility(View.INVISIBLE);
            }

            return row;

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
