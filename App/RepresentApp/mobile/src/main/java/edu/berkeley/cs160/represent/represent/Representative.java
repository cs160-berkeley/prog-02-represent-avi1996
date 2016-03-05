package edu.berkeley.cs160.represent.represent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Avi on 3/2/16.
 */
public class Representative {

    public static final int TYPE_SENATOR = 0;
    public static final int TYPE_HOUSE_REP = 1;

    public static final String TYPE_SENATOR_STRING = "Senator";
    public static final String TYPE_HOUSE_STRING = "Representative";

    public static final int PARTY_DEMOCRAT = 0;
    public static final int PARTY_REPUBLICAN = 1;
    public static final int PARTY_INDEPENDENT = 2;

    public static final String PARTY_DEMOCRAT_STRING = "Democrat";
    public static final String PARTY_REPUBLICAN_STRING = "Republican";
    public static final String PARTY_INDEPENDENT_STRING = "Independent";

    private static HashMap<String, Representative> representativeHashMap = new HashMap<>();

    private int type;
    private String name;
    private String email;
    private String website;
    private String twitterHandle;
    private String lastTweet;
    private String picUrl;
    private int party;

    public String getPartyText() {
        return partyText;
    }

    public void setPartyText(String partyText) {
        this.partyText = partyText;
    }

    private String partyText;
    private String endOfTerm;
    private ArrayList<String> committeesList;
    private ArrayList<String> sponsoredBillsList;

    public Representative(int type, String name, String email, String website, String twitterHandle,
                          String lastTweet, String picUrl, int party, String endOfTerm,
                          ArrayList<String> committeesList, ArrayList<String> sponsoredBillsList) {
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

    public static HashMap<String, Representative> getRepresentativeHashMap() {
        return representativeHashMap;
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

    public String getLastTweet() {
        return lastTweet;
    }

    public void setLastTweet(String lastTweet) {
        this.lastTweet = lastTweet;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
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

    public ArrayList<String> getSponsoredBillsList() {
        return sponsoredBillsList;
    }

    public void setSponsoredBillsList(ArrayList<String> sponsoredBillsList) {
        this.sponsoredBillsList = sponsoredBillsList;
    }
}
