package edu.berkeley.cs160.represent.represent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;


/**
 * Created by Avi on 3/3/16.
 */
public class WatchListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks {
    // In PhoneToWatchService, we passed in a path, either "/FRED" or "/LEXY"
    // These paths serve to differentiate different phone-to-watch messages
    private static final String FRED_FEED = "/Fred";
    private static final String LEXY_FEED = "/Lexy";
    private static final String TAG = "WatchListenerService";

    private GoogleApiClient mGoogleApiClient;


    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Service created");

        mGoogleApiClient = new GoogleApiClient.Builder( this )
                .addApiIfAvailable(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
        //and actually connect it
        mGoogleApiClient.connect();
    }


    private static final String WEARABLE_DATA_PATH = "/wearable_data";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged");
        DataMap dataMap;
        for (DataEvent event : dataEvents) {
            Log.d(TAG, "checking data type");
            // Check the data type
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(WEARABLE_DATA_PATH)) {}
                dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                Log.v(TAG, "DataMap received on watch: " + dataMap);
                Intent intent = new Intent(this, RepresentativesActivity.class );
                intent.putExtra("data", dataMap.toBundle());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }
}
