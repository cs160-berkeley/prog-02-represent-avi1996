package edu.berkeley.cs160.represent.represent;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RepresentativesActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, View.OnClickListener, SensorEventListener {


    private static final String TAG = "RepresentativesActivity";
//    private ImageView mRepImage;
//    private TextView mRepName;
    private RelativeLayout mRepSection;
    private GoogleApiClient mGoogleApiClient;
    private List<Node> nodes;

    private ListView mCardScrollView;

    private float swipeLeft = 0;

    private static final float SHAKE_THRESHOLD = 1.2f;
    private static final int SHAKE_WAIT_TIME_MS = 1000;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private long mShakeTime = 0;

    private String county;
    private String barack;
    private String mitt;
    private int repCount;

    public static final String PARTY_DEMOCRAT_STRING = "Democrat";
    public static final String PARTY_REPUBLICAN_STRING = "Republican";
    public static final String PARTY_INDEPENDENT_STRING = "Independent";

    private ArrayList<String> mReps;

    GridViewPager pager;
    String location;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_representatives);
        pager = (GridViewPager) findViewById(R.id.pager);

        mGoogleApiClient = new GoogleApiClient.Builder( this )
                .addApiIfAvailable(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();

        Bundle data = getIntent().getBundleExtra("data");
        if (data != null) {
            repCount = Integer.parseInt(data.getString("rep_count"));
            county = data.getString("county_name");
            barack = data.getString("barack");
            mitt = data.getString("mitt");

            mReps = new ArrayList<>();
            for (Integer i = 0; i < repCount; i++) {
                mReps.add(data.getString(i.toString()));
                Log.d(TAG, "REP " + mReps.get(i));
            }

            pager.setAdapter(new RepCardAdapter(this, getFragmentManager()));
        }

        pager.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                transitionToVoteActivity();
                return false;
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onConnected(Bundle bundle) {

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                nodes = getConnectedNodesResult.getNodes();
                Log.d(TAG, "found nodes");
                for (Node n : nodes) {
                    Log.d(TAG, n.getDisplayName());
                }
            }
        });

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rep_section:
                Log.d(TAG, "Rep Section CLicked");
                break;
        }
    }

    private void sendMessage(final String path, String text) {
        if (text == null) {
            text = "";
        }
        for (Node node : nodes) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node.getId(), path, text.getBytes());
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        mSensorManager.unregisterListener(this);
    }


    private class RepCardAdapter extends FragmentGridPagerAdapter {

        private final Context mContext;
        private ArrayList<String> mRowsList;
        private HashMap<String, String> mRows;
        private ArrayList<ColorDrawable> mColors;

        private int currentRow;


        public RepCardAdapter(Context context, FragmentManager fm) {
            super(fm);

            mContext = context;

            mRowsList = new ArrayList<>();
            mColors = new ArrayList<>();

            for (String s: mReps) {
                String[] terms = s.split(" ");
                String name = terms[1] + " " + terms[2] + " " + terms[3];
                mRowsList.add(name);

                String party = terms[0];

                if (party.equals(PARTY_DEMOCRAT_STRING)) {
                    mColors.add(new ColorDrawable(ContextCompat.getColor(mContext, R.color.democrat)));
                } else if (party.equals(PARTY_REPUBLICAN_STRING)) {
                    mColors.add(new ColorDrawable(ContextCompat.getColor(mContext, R.color.republican)));
                } else if (party.equals(PARTY_INDEPENDENT_STRING)) {
                    mColors.add(new ColorDrawable(ContextCompat.getColor(mContext, R.color.independent)));
                }
            }
        }


        @Override
        public Fragment getFragment(int i, int i1) {
            return cardFragment(i);
        }

        private Fragment cardFragment(int rowNum) {
            CardFragment fragment = CardFragment.create("", mRowsList.get(rowNum));
            // Add some extra bottom margin to leave room for the page indicator
            return fragment;
        }

        @Override
        public int getRowCount() {
            return mReps.size();
        }

        @Override
        public int getColumnCount(int i) {
            return 1;
        }


        @Override
        public Drawable getBackgroundForRow(final int row) {
            return mColors.get(row);
        }

    }

    private void transitionToVoteActivity() {
        Intent i = new Intent(this, VoteActivity.class);
        i.putExtra("county_name", county);
        i.putExtra("obama", barack);
        i.putExtra("mitt", mitt);
        startActivity(i);
        finish();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // If sensor is unreliable, then just return
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
        {
            return;
        }

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            detectShake(event);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // References:
    //  - http://jasonmcreynolds.com/?p=388
    //  - http://code.tutsplus.com/tutorials/using-the-accelerometer-on-android--mobile-22125
    private void detectShake(SensorEvent event) {
        long now = System.currentTimeMillis();

        if((now - mShakeTime) > SHAKE_WAIT_TIME_MS) {
            mShakeTime = now;

            float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
            float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
            float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;

            // gForce will be close to 1 when there is no movement
            double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            // Change background color if gForce exceeds threshold;
            // otherwise, reset the color
            if(gForce > SHAKE_THRESHOLD) {
                Log.d(TAG, "WATCH SHAKEN");
                sendMessage("/random_location", null);
            }
            else {
            }
        }

    }


}
