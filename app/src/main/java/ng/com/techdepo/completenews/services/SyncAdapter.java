package ng.com.techdepo.completenews.services;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import ng.com.techdepo.completenews.Utils;
import ng.com.techdepo.completenews.net.RSSItem;
import ng.com.techdepo.completenews.net.RSSParser;
import ng.com.techdepo.completenews.provider.FeedContract;

/**
 * Created by ESIDEM jnr on 2/21/2017.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter{

    public static final String TAG = "SyncAdapter";



    RSSParser rssParser = new RSSParser();

    List<RSSItem> rssItems = new ArrayList<RSSItem>();








    private static final String[] PROJECTION = new String[] {
            FeedContract.Entry._ID,
            FeedContract.Entry.COLUMN_NAME_GUID,
            FeedContract.Entry.COLUMN_NAME_TITLE,
            FeedContract.Entry.COLUMN_NAME_LINK,
            FeedContract.Entry.COLUMN_NAME_PUBLISHED,
            FeedContract.Entry.COLUMN_NAME_DESCRIPTION,
            FeedContract.Entry.COLUMN_NAME_IMAGE_URL};



    // Constants representing column positions from PROJECTION.
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_ENTRY_ID = 1;
    public static final int COLUMN_TITLE = 2;
    public static final int COLUMN_LINK = 3;
    public static final int COLUMN_PUBLISHED = 4;



    /**
     * URL to fetch content from during a sync.
     *
     * Network connection timeout, in milliseconds.
     */
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds

    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds

    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;


    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Called by the Android system in response to a request to run the sync adapter. The work
     * required to read data from the network, parse it, and store it in the content provider is
     * done here. Extending AbstractThreadedSyncAdapter ensures that all methods within SyncAdapter
     * run on a background thread. For this reason, blocking I/O and other long-running tasks can be
     * run <em>in situ</em>, and you don't have to set up a separate thread for them.
     .
     *
     * <p>This is where we actually perform any work required to perform a sync.
     * {@link android.content.AbstractThreadedSyncAdapter} guarantees that this will be called on a non-UI thread,
     * so it is safe to peform blocking I/O here.
     *
     * <p>The syncResult argument allows you to pass information back to the method that triggered
     * the sync.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        new loadRSSFeedItems().execute();
    }


    class loadRSSFeedItems extends AsyncTask<Void, String, String> {


        @Override
        protected String doInBackground(Void... voids) {

            rssItems = rssParser.parse();

            // Build hash table of incoming entries
            HashMap<String, RSSItem> entryMap = new HashMap<String, RSSItem>();

            mContentResolver.delete(FeedContract.Entry.CONTENT_URI,null,null);

            // looping through each item
            for(RSSItem item : rssItems){

                entryMap.put(item.getGuid(), item);

                insertEntry(item);


            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String args) {
            // dismiss the dialog after getting all products
//            progressDialog.dismiss();
        }
    }

    private void insertEntry(RSSItem entry) {




        ContentValues values = new ContentValues();
        values.clear();
        values.put(FeedContract.Entry.COLUMN_NAME_GUID, entry.getGuid());
        values.put(FeedContract.Entry.COLUMN_NAME_TITLE, entry.getTitle());
        values.put(FeedContract.Entry.COLUMN_NAME_LINK, entry.getLink());
        values.put(FeedContract.Entry.COLUMN_NAME_DESCRIPTION, entry.getDescription());
        values.put(FeedContract.Entry.COLUMN_NAME_IMAGE_URL, entry.getImageUrl());
        values.put(FeedContract.Entry.COLUMN_NAME_PUBLISHED, entry.getPubDate());
        values.put(FeedContract.Entry.COLUMN_NAME_COMPLETE_DES, entry.getCom_description());
        values.put(FeedContract.Entry.COLUMN_NAME_IMAGE_URL_2, entry.getImageUrl2());

        mContentResolver.insert(FeedContract.Entry.CONTENT_URI, values);
    }


}
