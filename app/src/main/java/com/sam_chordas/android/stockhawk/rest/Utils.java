package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.model.HistoricalData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static ArrayList quoteJsonToContentVals(String JSON) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");
                    ContentProviderOperation contentProviderOperation = buildBatchOperation(jsonObject);
                    if (contentProviderOperation != null)
                        batchOperations.add(contentProviderOperation);
                    else
                        return null;
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildBatchOperation(jsonObject));
                        }
                    }
                }
            }
        } catch (JSONException e) {
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) {
        bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format("%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            String change = jsonObject.getString("Change");
            String symbol = jsonObject.getString("symbol");
            String changeInPrice = jsonObject.getString("ChangeinPercent");
            String bid = jsonObject.getString("Bid");

            if ((!change.equals("null")) && (!changeInPrice.equals("null")) && (!bid.equals("null"))) {
                builder.withValue(QuoteColumns.SYMBOL, symbol);
                builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(bid));
                builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                        changeInPrice, true));
                builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
                builder.withValue(QuoteColumns.ISCURRENT, 1);
                if (change.charAt(0) == '-') {
                    builder.withValue(QuoteColumns.ISUP, 0);
                } else {
                    builder.withValue(QuoteColumns.ISUP, 1);
                }
            } else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    static public void setRequestStatus(Context c) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
        boolean i = PreferenceManager.getDefaultSharedPreferences(c).getBoolean(c.getString(R.string.pre_request_status), false);
        editor.putBoolean(c.getString(R.string.pre_request_status), !i);
        editor.commit();
    }

    public static ArrayList getHistoricalData(String JSON) {
        ArrayList<HistoricalData> historicalData = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                if (resultsArray != null && resultsArray.length() != 0) {
                    for (int i = 0; i < resultsArray.length(); i++) {
                        jsonObject = resultsArray.getJSONObject(i);
                        historicalData.add(new HistoricalData(
                                jsonObject.getString("Symbol"),
                                jsonObject.getString("Date"),
                                Float.parseFloat(jsonObject.getString("Open")),
                                Float.parseFloat(jsonObject.getString("Close")),
                                Float.parseFloat(jsonObject.getString("High")),
                                Float.parseFloat(jsonObject.getString("Low")),
                                Integer.parseInt(jsonObject.getString("Volume")),
                                Float.parseFloat(jsonObject.getString("Adj_Close"))));
                    }
                }
            }
        } catch (JSONException e) {
        }
        return historicalData;
    }
}
