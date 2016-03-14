package edu.berkeley.cs160.represent.represent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.berkeley.cs160.represent.represent.utils.NetworkUtils;

public class PickLocationActivity extends RepresentActivity implements View.OnClickListener, TextView.OnEditorActionListener {

    private static final String TAG = "PickLocationActivity";

    private EditText mEnterLocation;
    private RelativeLayout mGetLocation;

    private List<Node> nodes = new ArrayList<>();

    private Location mLastLocation;
    private static Activity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_location);

        activity = this;

        mEnterLocation = (EditText) findViewById(R.id.search_location);
        mEnterLocation.setOnEditorActionListener(this);
        mGetLocation = (RelativeLayout) findViewById(R.id.get_location_section);
        mGetLocation.setOnClickListener(this);

        boolean random = getIntent().getBooleanExtra("random_location", false);
        if (random) {
            mLastLocation = new Location("");
            Double lat = getIntent().getDoubleExtra("lat", 0);
            Double lng = getIntent().getDoubleExtra("lng", 0);
            Log.d(TAG, lat.toString());
            Log.d(TAG, lng.toString());
            mLastLocation.setLatitude(lat.doubleValue());
            mLastLocation.setLongitude(lng.doubleValue());
            onClick(mGetLocation);
        }

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
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d(TAG, "LATITUDE " + ((Double) mLastLocation.getLatitude()).toString());
            Log.d(TAG, "LONGITUDE " + ((Double) mLastLocation.getLongitude()).toString());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.get_location_section:

                Representative.clearRepresentativeHashMap();
                new ReverseGeoCode("get").execute(mLastLocation);
                break;

        }
    }

    private class GeoCodeZip extends AsyncTask<String, Void, String> {

        String url = "https://maps.googleapis.com/maps/api/geocode/json?components=postal_code:";
        String zip;

        @Override
        protected String doInBackground(String... params) {
            zip = params[0];
            try {
                return NetworkUtils.sendGET(url + zip);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Gson gson = new Gson();
            HashMap map = gson.fromJson(s, HashMap.class);
            ArrayList results = (ArrayList) map.get("results");
            Double lat = ((Double) ((LinkedTreeMap) ((LinkedTreeMap) (((LinkedTreeMap) results.get(0)).get("geometry"))).get("location")).get("lat"));
            Double lng = ((Double) ((LinkedTreeMap) ((LinkedTreeMap) (((LinkedTreeMap) results.get(0)).get("geometry"))).get("location")).get("lng"));

            mLastLocation.setLatitude(lat);
            mLastLocation.setLongitude(lng);
            Log.d(TAG, lat + " " + lng);
            new ReverseGeoCode("search", zip).execute(mLastLocation);
        }
    }

    private class ReverseGeoCode extends AsyncTask<Location, Void, String> {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=";
        String locRequestType;
        String zip;
        Location loc;

        public ReverseGeoCode(String... s) {
            locRequestType = s[0];
            if (locRequestType.equals("search")) {
                zip = s[1];
            }
        }

        @Override
        protected String doInBackground(Location... params) {
            Location loc = params[0];
            this.loc = loc;
            try {
                return NetworkUtils.sendGET(url + ((Double) loc.getLatitude()).toString() + "," + ((Double) loc.getLongitude()).toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Gson gson = new Gson();
            HashMap map = gson.fromJson(s, HashMap.class);
            ArrayList results = (ArrayList) map.get("results");
            LinkedTreeMap temp = ((LinkedTreeMap) ((ArrayList) ((LinkedTreeMap) results.get(0)).get("address_components")).get(3));
            String city = ((String) ((LinkedTreeMap) ((ArrayList) ((LinkedTreeMap) results.get(0)).get("address_components")).get(2)).get("short_name"));
            String state = ((String) ((LinkedTreeMap) ((ArrayList) ((LinkedTreeMap) results.get(0)).get("address_components")).get(4)).get("short_name"));
            String county = ((String) temp.get("short_name"));
            Bundle extras = new Bundle();
            if (locRequestType.equals("search")) {
                extras.putString("zip", zip);
                extras.putInt("request_type", 0);
            } else {
                extras.putDouble("lat", loc.getLatitude());
                extras.putDouble("lng", loc.getLongitude());
                extras.putInt("request_type", 1);
            }
            Representative.setCounty(county.split(" County")[0], activity, extras, city + " " + state);
        }
    }



    private void sendMessage(final String path, final String text ) {
        for (Node node : nodes) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node.getId(), path, text.getBytes());
        }

    }
    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            // loading of the bitmap was a success
            // TODO do some action with the bitmap

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            Asset asset = Asset.createFromBytes(baos.toByteArray());
            PutDataMapRequest dataMap = PutDataMapRequest.create("/rep");
            dataMap.getDataMap().putAsset("profileImage", asset);
            dataMap.getDataMap().putString("repName", "Senator David Perdue");
            dataMap.setUrgent();
            PutDataRequest request = dataMap.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                    .putDataItem(mGoogleApiClient, request);
            pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(final DataApi.DataItemResult result) {
                    if(result.getStatus().isSuccess()) {
                        Log.d(TAG, "Data item set: " + result.getDataItem().getUri());
                    }
                }
            });

//            transitionToRepsActivity(RepsListActivity.TYPE_GET_LOCATION, null);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            // loading of the bitmap failed
            // TODO do some action/warning/error message
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (v.getId() == R.id.search_location) {
            if (actionId == EditorInfo.IME_ACTION_GO) {
//                    sendMessage("/location", mEnterLocation.getText().toString());
//                    transitionToRepsActivity(RepsListActivity.TYPE_SEARCH_LOCATION, v.getText().toString());
                Representative.clearRepresentativeHashMap();
                new GeoCodeZip().execute(v.getText().toString());
            }
        }
        return false;
    }

    private class ImageDownloadTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {


            return null;

        }
    }


}
