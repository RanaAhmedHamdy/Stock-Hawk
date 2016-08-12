package com.sam_chordas.android.stockhawk.ui;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.model.HistoricalData;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsActivityFragment extends Fragment {

    private String symbol;
    private String startDate;
    private String endDate;
    private LineChartView lineChartView;
    private OkHttpClient client = new OkHttpClient();

    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public DetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        symbol = getActivity().getIntent().getExtras().getString("symbol");
        startDate = getActivity().getIntent().getExtras().getString("startDate");
        endDate = getActivity().getIntent().getExtras().getString("endDate");

        lineChartView = (LineChartView) view.findViewById(R.id.linechart);

        new FetchDetailsTask().execute();
        return view;
    }

    public class FetchDetailsTask extends AsyncTask<Void, Void, ArrayList<HistoricalData>> {

        @Override
        protected ArrayList<HistoricalData> doInBackground(Void... params) {
            StringBuilder urlStringBuilder = new StringBuilder();
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22" +
                    symbol + "%22%20and%20startDate%20%3D%20%22" +
                    startDate + "%22%20and%20endDate%20%3D%20%22" +
                    endDate + "%22");

            // finalize the URL for the API query.
            urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                    + "org%2Falltableswithkeys&callback=");

            String urlString;
            String getResponse;

            if (urlStringBuilder != null) {
                urlString = urlStringBuilder.toString();
                Log.i("search url", urlString);
                try {
                    getResponse = fetchData(urlString);
                    return Utils.getHistoricalData(getResponse);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<HistoricalData> historicalData) {
            super.onPostExecute(historicalData);

            ArrayList<Float> high = new ArrayList<>();
            float[] highArr = new float[historicalData.size()];
            String[] labelArr = new String[historicalData.size()];

            for (int i = 0; i < historicalData.size(); i++) {
                high.add(historicalData.get(i).getHigh());
                highArr[i] = historicalData.get(i).getHigh();
                labelArr[i] = historicalData.get(i).getDate();
            }

            LineSet dataset = new LineSet(labelArr, highArr);

            dataset.setColor(Color.parseColor("#FF00BF"))
                    .setDotsColor(Color.parseColor("#FF00BF"))
                    .setThickness(4);

            Paint gridPaint = new Paint();
            gridPaint.setColor(Color.parseColor("#FFFFFF"));
            gridPaint.setStyle(Paint.Style.FILL);
            gridPaint.setAntiAlias(true);
            gridPaint.setStrokeWidth(Tools.fromDpToPx(.1f));

            lineChartView
                    .setGrid(ChartView.GridType.FULL, gridPaint)
                    .setAxisBorderValues(Collections.min(high).intValue(), Collections.max(high).intValue() + 1)
                    .setLabelsColor(Color.parseColor("#FFFFFF"))
                    .setYLabels(AxisController.LabelPosition.OUTSIDE);

            lineChartView.addData(dataset);
            lineChartView.show();
        }
    }
}
