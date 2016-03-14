package edu.berkeley.cs160.represent.represent;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import edu.berkeley.cs160.represent.represent.apis.SunlightAPI;

/**
 * Created by Avi on 3/2/16.
 */
public class Representative {

    public static final int TYPE_SENATOR = 0;
    public static final int TYPE_HOUSE_REP = 1;
    public static final int TYPE_NOT_REAL = 5;

    public static final String TYPE_SENATOR_STRING = "Senator";
    public static final String TYPE_HOUSE_STRING = "Representative";

    public static final int PARTY_DEMOCRAT = 0;
    public static final int PARTY_REPUBLICAN = 1;
    public static final int PARTY_INDEPENDENT = 2;

    public static final String PARTY_DEMOCRAT_STRING = "Democrat";
    public static final String PARTY_REPUBLICAN_STRING = "Republican";
    public static final String PARTY_INDEPENDENT_STRING = "Independent";
    private static final String TAG = "Representative";

    private static HashMap<String, Representative> representativeHashMap = new HashMap<>();
    private static ArrayList<Representative> senators = new ArrayList<>();
    private static ArrayList<Representative> representatives = new ArrayList<>();
    private static int fakeIdIter = 0;
    private static String county = "";
    private static Double obama;
    private static Double mitt;
    private int type;
    private String name;
    private String email;
    private String website;
    private String twitterHandle;
    private long lastTweet;
    private String picUrl;
    private int party;
    private String partyText;
    private String endOfTerm;
    private ArrayList<String> committeesList;
    private HashMap<String, String> sponsoredBillsList;
    private String bioguideID;
    private int fakeId;

    public Representative(int type, String name, String email, String website, String twitterHandle, int party, String endOfTerm) {
        this.type = type;
        if (type == TYPE_SENATOR) {
            senators.add(this);
        } else if (type == TYPE_HOUSE_REP) {
            representatives.add(this);
        }

        this.name = name;
        this.email = email;
        this.website = website;
        this.twitterHandle = twitterHandle;
        this.party = party;
        this.endOfTerm = endOfTerm;
        if (party == PARTY_DEMOCRAT) {
            this.partyText = PARTY_DEMOCRAT_STRING;
        } else if (party == PARTY_INDEPENDENT) {
            this.partyText = PARTY_INDEPENDENT_STRING;
        } else if (party == PARTY_REPUBLICAN) {
            this.partyText = PARTY_REPUBLICAN_STRING;
        }

        committeesList = new ArrayList<>();
        sponsoredBillsList = new HashMap<>();

        representativeHashMap.put(name, this);
    }
    public Representative(int type, String name, String email, String website, String twitterHandle,
                          long lastTweet, String picUrl, int party, String endOfTerm,
                          ArrayList<String> committeesList, HashMap<String, String> sponsoredBillsList) {
        this.type = type;
        this.name = name;
        this.email = email;
        this.website = website;
        this.twitterHandle = twitterHandle;
        this.lastTweet = lastTweet;
        this.picUrl = picUrl;
        this.party = party;
        if (party == PARTY_DEMOCRAT) {
            this.partyText = PARTY_DEMOCRAT_STRING;
        } else if (party == PARTY_INDEPENDENT) {
            this.partyText = PARTY_INDEPENDENT_STRING;
        } else if (party == PARTY_REPUBLICAN) {
            this.partyText = PARTY_REPUBLICAN_STRING;
        }

        this.endOfTerm = endOfTerm;
        this.committeesList = committeesList;
        this.sponsoredBillsList = sponsoredBillsList;

        representativeHashMap.put(name, this);
    }

    public Representative() {
        this.type = TYPE_NOT_REAL;
        this.fakeId = fakeIdIter;
        fakeIdIter++;
    }

    public static Double getObama() {
        return obama;
    }

    public static Double getMitt() {
        return mitt;
    }

    public static ArrayList<Representative> getSenators() {
        return senators;
    }

    public static ArrayList<Representative> getRepresentatives() {
        return representatives;
    }

    public static HashMap<String, Representative> getRepresentativeHashMap() {
        return representativeHashMap;
    }

    public static void clearRepresentativeHashMap() {
        representativeHashMap.clear();
        senators.clear();
        representatives.clear();
    }

    public static String getCounty() {
        return county;
    }

    public static void setCounty(String county, Activity activity, Bundle bundle, String place) {
        try {
            InputStream is = activity.getAssets().open("election-county-2012.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonString = new String(buffer, "UTF-8");
            Gson gson = new Gson();
            ArrayList map = gson.fromJson(jsonString, ArrayList.class);
            int i = 0;
            for (Object o: map) {
                if (((String) ((LinkedTreeMap) o).get("county-name")).equals(county)) {
                    obama = ((Double) ((LinkedTreeMap) o).get("obama-percentage"));
                    mitt = ((Double) ((LinkedTreeMap) o).get("romney-percentage"));
                    Log.d(TAG, "OBAMA PERCENTAGE " + obama.toString());
                    Log.d(TAG, "MITT PERCENTAGE " + mitt.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Representative.county = county;
        if (bundle.getInt("request_type") == 0) {
            SunlightAPI.getRepresentatives(bundle.getString("zip"), activity);
        } else {
            SunlightAPI.getRepresentatives(bundle.getDouble("lat"), bundle.getDouble("lng"), activity, place);
        }

    }

    public HashMap<String, String> getSponsoredBillsList() {
        return sponsoredBillsList;
    }

    public void setSponsoredBillsList(HashMap<String, String> sponsoredBillsList) {
        this.sponsoredBillsList = sponsoredBillsList;
    }

    public String getPartyText() {
        return partyText;
    }

    public void setPartyText(String partyText) {
        this.partyText = partyText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Representative that = (Representative) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return !(email != null ? !email.equals(that.email) : that.email != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getTwitterHandle() {
        return twitterHandle;
    }

    public void setTwitterHandle(String twitterHandle) {
        this.twitterHandle = twitterHandle;
    }

    public long getLastTweet() {
        return lastTweet;
    }

    public void setLastTweet(long lastTweet) {
        this.lastTweet = lastTweet;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        String url = picUrl.substring(0, picUrl.lastIndexOf('.'));
        String extension = picUrl.substring(picUrl.lastIndexOf('.'));
        this.picUrl = url.substring(0, url.lastIndexOf('_')) + extension;
        Log.d(TAG, this.picUrl);
    }

    public int getParty() {
        return party;
    }

    public void setParty(int party) {
        this.party = party;
    }

    public String getEndOfTerm() {
        return endOfTerm;
    }

    public void setEndOfTerm(String endOfTerm) {
        this.endOfTerm = endOfTerm;
    }

    public ArrayList<String> getCommitteesList() {
        return committeesList;
    }

    public void setCommitteesList(ArrayList<String> committeesList) {
        this.committeesList = committeesList;
    }


    public String getBioguideID() {
        return bioguideID;
    }

    public void setBioguideID(String bioguideID) {
        this.bioguideID = bioguideID;
    }

    public int getFakeId() {
        return fakeId;
    }
}
