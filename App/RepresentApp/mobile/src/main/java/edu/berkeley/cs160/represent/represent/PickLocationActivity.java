package edu.berkeley.cs160.represent.represent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PickLocationActivity extends RepresentActivity implements View.OnClickListener {

    private static final String TAG = "PickLocationActivity";

    private boolean wearConnected;

    private EditText mEnterLocation;
    private RelativeLayout mGetLocation;

    private List<Node> nodes = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_location);

        wearConnected = false;

        mEnterLocation = (EditText) findViewById(R.id.search_location);
        mEnterLocation.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
//                    sendMessage("/location", mEnterLocation.getText().toString());
                    transitionToRepsActivity(RepsListActivity.TYPE_SEARCH_LOCATION, v.getText().toString());
                }
                return false;
            }
        });

        mGetLocation = (RelativeLayout) findViewById(R.id.get_location_section);
        mGetLocation.setOnClickListener(this);

        mGoogleApiClient.connect();

    }

    @Override
    public void onConnected(Bundle bundle) {
        wearConnected = true;

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
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.get_location_section:
                sendMessage("/location", "30005");
                transitionToRepsActivity(RepsListActivity.TYPE_GET_LOCATION, null);

//                new ImageDownloadTask().execute();
//                Picasso.with(getApplicationContext())
//                        .load("https://upload.wikimedia.org/wikipedia/commons/5/53/David_Perdue_official_Senate_photo.jpg")
//                        .into(target);

                break;

        }
    }

    private void transitionToRepsActivity(int type, String zip) {
        Intent i = new Intent(this, RepsListActivity.class);
        i.putExtra("type", type);
        if (zip != null) {
            i.putExtra("zip", zip);
        }
        startActivity(i);
        finish();
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

            transitionToRepsActivity(RepsListActivity.TYPE_GET_LOCATION, null);
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

    private class ImageDownloadTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {


            return null;

        }
    }


}
