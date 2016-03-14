package edu.berkeley.cs160.represent.represent;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

public class VoteActivity extends Activity {

    private TextView mLocation;
    private TextView mBarack;
    private TextView mMitt;

    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        location = getIntent().getStringExtra("location");
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mLocation = (TextView) stub.findViewById(R.id.location);
                mBarack = (TextView) stub.findViewById(R.id.brobama);
                mMitt = (TextView) stub.findViewById(R.id.mitt);

                mLocation.setText(getIntent().getStringExtra("county_name"));
                mBarack.setText(getIntent().getStringExtra("obama"));
                mMitt.setText(getIntent().getStringExtra("mitt"));

            }
        });
    }
}
