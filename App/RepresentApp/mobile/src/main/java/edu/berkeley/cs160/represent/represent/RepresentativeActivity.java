package edu.berkeley.cs160.represent.represent;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RepresentativeActivity extends AppCompatActivity {

    private Representative rep;

    private RelativeLayout mRepBackground;
    private ImageView mRepImage;
    private TextView mPartyName;
    private TextView mEODText;
    private TextView mCommitteesText;
    private TextView mBillsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_representative);

        rep = Representative.getRepresentativeHashMap().get(getIntent().getStringExtra("rep"));

        if (rep.getType() == Representative.TYPE_SENATOR) {
            getSupportActionBar().setTitle("Senator " + rep.getName());
        } else {
            getSupportActionBar().setTitle("Representative " + rep.getName());
        }

        mRepBackground = (RelativeLayout) findViewById(R.id.representative_background);
        mRepImage = (ImageView) findViewById(R.id.rep_image);
        mPartyName = (TextView) findViewById(R.id.rep_party_text);
        mEODText = (TextView) findViewById(R.id.rep_eod_text);
        mCommitteesText = (TextView) findViewById(R.id.rep_committees);
        mBillsText = (TextView) findViewById(R.id.rep_bills);

        ImageUtils.loadImageFromUrl(this, rep.getPicUrl(), mRepImage);

        switch (rep.getParty()) {
            case Representative.PARTY_DEMOCRAT:
                mRepBackground.setBackground(ContextCompat.getDrawable(this, R.drawable.reps_shape_democrat));
                break;
            case Representative.PARTY_REPUBLICAN:
                mRepBackground.setBackground(ContextCompat.getDrawable(this, R.drawable.reps_shape_republican));
                break;
            case Representative.PARTY_INDEPENDENT:
                mRepBackground.setBackground(ContextCompat.getDrawable(this, R.drawable.reps_shape_independent));
                break;
        }

        mPartyName.setText("Party: " + rep.getPartyText());
        mEODText.setText("End of Term: " + rep.getEndOfTerm());
        String committees = "Committees: \n";
        for (Integer i = 1; i <= rep.getCommitteesList().size(); i++) {
            committees += i.toString() + ". " + rep.getCommitteesList().get(i - 1);
            if (i != rep.getCommitteesList().size()) {
                committees += "\n";
            }
        }
        mCommitteesText.setText(committees);
        String bills = "Sponsored Bills: \n";
        for (Integer i = 1; i <= rep.getSponsoredBillsList().size(); i++) {
            bills += i.toString() + ". " + rep.getSponsoredBillsList().get(i - 1) + "\n";
            if (i != rep.getSponsoredBillsList().size()) {
                committees += "\n";
            }
        }
        mBillsText.setText(bills);

    }
}
