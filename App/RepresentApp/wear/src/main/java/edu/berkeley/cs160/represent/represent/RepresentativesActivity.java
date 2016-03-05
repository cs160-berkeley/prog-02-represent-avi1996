package edu.berkeley.cs160.represent.represent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RepresentativesActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, DataApi.DataListener, View.OnClickListener, SensorEventListener {

    private static final String TAG = "RepresentativesActivity";
//    private ImageView mRepImage;
//    private TextView mRepName;
    private RelativeLayout mRepSection;
    private GoogleApiClient mGoogleApiClient;
    private List<Node> nodes;

    private ListView mCardScrollView;

    private float swipeLeft = 0;

    private static final float SHAKE_THRESHOLD = 1.5f;
    private static final int SHAKE_WAIT_TIME_MS = 1000;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private long mShakeTime = 0;

    String location;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_representatives);
//        final BoxInsetLayout stub = (BoxInsetLayout) findViewById(R.id.card);
//        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
//            @Override
//            public void onLayoutInflated(WatchViewStub stub) {
//                mTextView = (TextView) stub.findViewById(R.id.text);
//            }
//        });

        mGoogleApiClient = new GoogleApiClient.Builder( this )
                .addApiIfAvailable(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
        //and actually connect it
        mGoogleApiClient.connect();

//        mRepSection = (RelativeLayout) findViewById(R.id.rep_section);
//        mRepImage = (ImageView) findViewById(R.id.rep_image);
//        mRepName = (TextView) findViewById(R.id.rep_name);
//
//
//        ImageUtils.loadImage(this, R.drawable.david_perdue, mRepImage);
//        mRepName.setText("Senator David Perdue");

//        mRepSection.setOnClickListener(this);

        mCardScrollView = (ListView) findViewById(R.id.card);

        location = getIntent().getStringExtra("location");

        RepCardAdapter adapter = new RepCardAdapter(this, R.layout.rep_card);
        adapter.add("");
        adapter.add("");
        adapter.add("");

        mCardScrollView.setAdapter(adapter);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

//        Bitmap image = getIntent().getParcelableExtra("rep_image");
//        String name = getIntent().getStringExtra("rep_name");
//
//        if (image != null && name != null) {
//            ImageUtils.loadImageFromBitmap(this, image, mRepImage);
//            mRepName.setText(name);
//        }

//        Intent sendIntent = new Intent(getBaseContext(), WatchListenerService.class);
//        startService(sendIntent);

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/rep")) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                Asset profileAsset = dataMapItem.getDataMap().getAsset("profileImage");
                Bitmap bitmap = loadBitmapFromAsset(profileAsset);
                String repName = dataMapItem.getDataMap().getString("repName");
                Intent intent = new Intent(this, RepresentativesActivity.class );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Log.d(TAG, "repName " + repName);
                intent.putExtra("rep_name", repName);
                intent.putExtra("rep_image", bitmap);
                startActivity(intent);
                // Do something with the bitmap
            }
        }
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                mGoogleApiClient.blockingConnect(1000, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
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
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rep_section:
                Log.d(TAG, "Rep Section CLicked");
                sendMessage("/rep", "David Perdue");
                break;
        }
    }

    private void sendMessage(final String path, final String text ) {

        for (Node node : nodes) {
            Log.d(TAG, "Sending message: " + text);
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node.getId(), path, text.getBytes());
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        mSensorManager.unregisterListener(this);
    }

    private class RepCardAdapter extends ArrayAdapter<String> implements View.OnClickListener {

        public RepCardAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.rep_card, parent, false);
            }

            ImageView mRepImage = (ImageView) row.findViewById(R.id.rep_image);
            TextView mRepName = (TextView) row.findViewById(R.id.rep_name);

            if (position == 0) {
                ImageUtils.loadImage(getContext(), R.drawable.johnny_isakson, mRepImage);
                mRepName.setText("Senator Johnny Isakson");
                row.setOnClickListener(null);
            } else if (position == 1) {

                ImageUtils.loadImage(getContext(), R.drawable.david_perdue, mRepImage);
                mRepName.setText("Senator David Perdue");
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "REP CARD ON CLICK");
                        sendMessage("/rep", "David Perdue");
                    }
                });

            } else if (position == 2) {

                ImageUtils.loadImage(getContext(), R.drawable.johnny_isakson, mRepImage);
                mRepName.setText("Representative Tom Price");
                row.setOnClickListener(null);
            }
            row.setOnDragListener(new View.OnDragListener() {
                @Override
                public boolean onDrag(View v, DragEvent event) {
                    Log.d(TAG, ((Float) swipeLeft).toString());
                    if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
                        swipeLeft = event.getX();
                    } else if (event.getAction() == DragEvent.ACTION_DRAG_ENDED) {
                        if (swipeLeft - event.getX() > 0) {
                            Log.d(TAG, "SWIPED LEFT");
                            swipeLeft = 0;
                        }
                    }
//                event.get
                    return false;
                }
            });
            row.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.d(TAG, "LONG CLICKED");
                    transitionToVoteActivity();
                    return false;
                }
            });
            return row;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.layout.rep_card:
                    Log.d(TAG, "REP CARD ON CLICK");
                    sendMessage("/rep", "David Perdue");
                    break;
            }
        }

    }

    private void transitionToVoteActivity() {
        Intent i = new Intent(this, VoteActivity.class);
        i.putExtra("location", location);
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
                Log.d(TAG, "REALLY SHOOK THAT BITCH");
                sendMessage("/new_location", randomLocationGenerator());
            }
            else {
//                Log.d(TAG, "KINDA SHOOK THAT BITCH");
            }
        }

    }

    private String randomLocationGenerator() {
        return "12345";
    }

}
