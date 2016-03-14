package edu.berkeley.cs160.represent.represent;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;

/**
 * Created by Avi on 3/3/16.
 */
public class PhoneListenerService extends WearableListenerService {

    //   WearableListenerServices don't need an iBinder or an onStartCommand: they just need an onMessageReceieved.
    private static final String TOAST = "/send_toast";
    private static final String TAG = "PhoneListenerService";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("T", "in PhoneListenerService, got: " + messageEvent.getPath());
        if( messageEvent.getPath().equalsIgnoreCase("/rep") ) {

            // Value contains the String we sent over in WatchToPhoneService, "good job"

            // Make a toast with the String
            String value = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            Intent intent = new Intent(this, RepresentativeActivity.class );
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.d(TAG, "VALUE IS " + value);
            intent.putExtra("rep", value);
            startActivity(intent);

            // so you may notice this crashes the phone because it's
            //''sending message to a Handler on a dead thread''... that's okay. but don't do this.
            // replace sending a toast with, like, starting a new activity or something.
            // who said skeleton code is untouchable? #breakCSconceptions

        } else if (messageEvent.getPath().equalsIgnoreCase("/random_location")) {
            Intent intent = new Intent(this, PickLocationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("random_location", true);
            double[] location = randomLocationGenerator();
            Log.d(TAG, "LAT " + ((Double) location[0]).toString());
            Log.d(TAG, "LONG " + ((Double) location[1]).toString());
            intent.putExtra("lat", location[0]);
            intent.putExtra("lng", location[1]);
            startActivity(intent);
        } else {
            super.onMessageReceived( messageEvent );
        }

    }

    private double[] randomLocationGenerator() {

        Double NORTHERN_BOUND = 41.04621681;
        Double SOUTHERN_BOUND = 33.13755119;
        Double EASTERN_BOUND = -82.08984375;
        Double WESTERN_BOUND = -117.09760666;

        double lat = (double) (Math.random() * (NORTHERN_BOUND - SOUTHERN_BOUND)) + SOUTHERN_BOUND;
        double lng = (double) (Math.random() * (EASTERN_BOUND - WESTERN_BOUND)) + WESTERN_BOUND;

        return new double[]{lat, lng};
    }
}

