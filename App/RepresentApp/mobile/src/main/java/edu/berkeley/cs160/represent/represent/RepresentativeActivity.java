package edu.berkeley.cs160.represent.represent;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import edu.berkeley.cs160.represent.represent.utils.ImageUtils;

public class RepresentativeActivity extends AppCompatActivity {

    private Representative rep;

    private RelativeLayout mRepBackground;
    private ImageView mRepImage;
    private TextView mPartyName;
    private TextView mEODText;
    private TextView mCommitteesText;
    private TextView mBillsText;
    private InfoAdapter mInfoAdapter;
    private ListView mInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_representative);

        rep = Representative.getRepresentativeHashMap().get(getIntent().getStringExtra("rep"));

        if (rep.getType() == Representative.TYPE_SENATOR) {
            getSupportActionBar().setTitle("Senator " + rep.getName());
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            getSupportActionBar().setTitle("Representative " + rep.getName());
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mRepBackground = (RelativeLayout) findViewById(R.id.representative_background);
        mRepImage = (ImageView) findViewById(R.id.rep_image);
        mPartyName = (TextView) findViewById(R.id.rep_party_text);
        mEODText = (TextView) findViewById(R.id.rep_eod_text);

        ImageUtils.loadImageFromUrlWithoutFitting(this, rep.getPicUrl(), mRepImage);

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

        mInfoAdapter = new InfoAdapter(this, R.layout.more_info_item);
        mInfoList = (ListView) findViewById(R.id.rep_info_list);

        mInfoAdapter.add("Committees~");
        for (String c: rep.getCommitteesList()) {
            mInfoAdapter.add(c + "~");
        }
        mInfoAdapter.add("Sponsored Bills~");
        for (String b: rep.getSponsoredBillsList().keySet()) {
            mInfoAdapter.add(b + "~" + rep.getSponsoredBillsList().get(b));
        }

        mInfoList.setAdapter(mInfoAdapter);

        mPartyName.setText("Party: " + rep.getPartyText());
        mEODText.setText("End of Term: " + convertDate(rep.getEndOfTerm()));

    }

    private class InfoAdapter extends ArrayAdapter<String> {

        private static final String TAG = "InfoAdapter";
        private ArrayList<String> informationList;

        public InfoAdapter(Context context, int resource) {
            super(context, resource);

            informationList = new ArrayList<>();
        }

        @Override
        public void add(String object) {
            super.add(object);

            informationList.add(object);
        }

        @Override
        public void addAll(Collection<? extends String> collection) {
            super.addAll(collection);

            informationList.addAll(collection);
        }

        @Override
        public String getItem(int position) {
            return informationList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.more_info_item, parent, false);
            }

            String item = getItem(position);
            String[] array = item.split("~");
            String text = array[0];
            String date = "";
            int type = 0;
            if (array.length == 2) {
                date = array[1];
                type = 1;
            }

            if (text.equals("Committees") || text.equals("Sponsored Bills")) {
                ((TextView) row.findViewById(R.id.category_text)).setText(text);
                ((RelativeLayout) row.findViewById(R.id.category_text_section)).setVisibility(View.VISIBLE);
                ((TextView) row.findViewById(R.id.more_info_text)).setVisibility(View.GONE);
                ((TextView) row.findViewById(R.id.more_info_subtitle)).setVisibility(View.GONE);
                (row.findViewById(R.id.info_item_background)).setVisibility(View.GONE);
            } else {
                if (type == 0) {
                    ((TextView) row.findViewById(R.id.more_info_text)).setText(text);
                    ((RelativeLayout) row.findViewById(R.id.category_text_section)).setVisibility(View.GONE);
                    ((TextView) row.findViewById(R.id.more_info_text)).setVisibility(View.VISIBLE);
                    ((TextView) row.findViewById(R.id.more_info_subtitle)).setVisibility(View.GONE);
                    (row.findViewById(R.id.info_item_background)).setVisibility(View.VISIBLE);
                } else if (type == 1) {
                    ((TextView) row.findViewById(R.id.more_info_text)).setText(text);
                    ((TextView) row.findViewById(R.id.more_info_text)).setMaxLines(2);
                    ((TextView) row.findViewById(R.id.more_info_text)).setEllipsize(TextUtils.TruncateAt.END);
                    ((TextView) row.findViewById(R.id.more_info_subtitle)).setText(convertDate(date));
                    ((RelativeLayout) row.findViewById(R.id.category_text_section)).setVisibility(View.GONE);
                    ((TextView) row.findViewById(R.id.more_info_text)).setVisibility(View.VISIBLE);
                    ((TextView) row.findViewById(R.id.more_info_subtitle)).setVisibility(View.VISIBLE);
                    (row.findViewById(R.id.info_item_background)).setVisibility(View.VISIBLE);
                }

            }

            return row;
        }
    }

    private String convertDate(String date) {
        String[] split = date.split("-");
        String toRet = split[2];

        int month = Integer.parseInt(split[1]);

        switch (month) {
            case 1:
                toRet += " JAN";
                break;
            case 2:
                toRet += " FEB";
                break;
            case 3:
                toRet += " MAR";
                break;
            case 4:
                toRet += " APR";
                break;
            case 5:
                toRet += " MAY";
                break;
            case 6:
                toRet += " JUNE";
                break;
            case 7:
                toRet += " JULY";
                break;
            case 8:
                toRet += " AUG";
                break;
            case 9:
                toRet += " SEPT";
                break;
            case 10:
                toRet += " OCT";
                break;
            case 11:
                toRet += " NOV";
                break;
            case 12:
                toRet += " DEC";
                break;
        }

        return toRet + " " + split[0];
    }

}
