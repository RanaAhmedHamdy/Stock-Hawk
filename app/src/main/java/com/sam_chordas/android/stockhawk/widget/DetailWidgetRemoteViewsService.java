package com.sam_chordas.android.stockhawk.widget;

/**
 * Created by Rana on 8/8/2016.
 */

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long token = Binder.clearCallingIdentity();
                try {
                    data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                            new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL,  QuoteColumns.PERCENT_CHANGE,
                                    QuoteColumns.CHANGE, QuoteColumns.BIDPRICE,
                                    QuoteColumns.ISUP},
                            QuoteColumns.ISCURRENT + " = ?",
                            new String[]{"1"},
                            null);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }

            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                String symbol = data.getString(data.getColumnIndex(QuoteColumns.SYMBOL));
                String bid = data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE));
                String change;
                if (Utils.showPercent){
                    change = data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
                } else {
                    change = data.getString(data.getColumnIndex(QuoteColumns.CHANGE));
                }

                views.setTextViewText(R.id.change_wd, change);
                views.setTextViewText(R.id.bid_price_wd, bid);
                views.setTextViewText(R.id.stock_symbol_wd, symbol);

                final Intent fillInIntent = new Intent();
                fillInIntent.setData(QuoteProvider.Quotes.CONTENT_URI);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}