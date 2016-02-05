package com.sivag1.juiceboard.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sivag1.juiceboard.BackendDataFacade;
import com.sivag1.juiceboard.data.JuiceLevel;
import com.sivag1.juiceboard.R;
import com.sivag1.juiceboard.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = SyncAdapter.class.getSimpleName();

    // Interval at which to sync with the weather, in milliseconds.
    // 1000 milliseconds (1 second) * 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 1;
    //public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private final Context mContext;
    private List<JuiceLevel> juiceLevelList = new ArrayList<JuiceLevel>();

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.d(LOG_TAG, "Creating SyncAdapter");
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Performing inside SyncAdapter");
        juiceLevelList.clear();
        BackendDataFacade.update(mContext, false, null);
        BackendDataFacade.fetchJuiceLevels(mContext, juiceLevelList);

        List<String> list = new ArrayList<String>();
        String strInterval = PreferenceManager.getDefaultSharedPreferences(mContext).getString
                ("sync_frequency",
                        "1");
        int interval = Integer.parseInt(strInterval) * 60 * 1000;
        for (JuiceLevel juiceLevel : juiceLevelList) {
            if (juiceLevel.getLastKnownPercentage() <= 15) {
                if (new Date().getTime() - juiceLevel.getLastUpdatedDate().getTime() <= interval) {
                    list.add(juiceLevel.getDeviceName());
                }
            }
        }

        if (list.size() == 0)
            return;

        StringBuffer sb = new StringBuffer(list.get(0));
        for (int i = 1; i < list.size(); i++) {
            sb.append(", " + list.get(i));
        }

        Utility.notify(mContext, sb.toString());
    }


    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context An app context
     */
    public static void syncImmediately(Context context) {

    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {

        Account account = getSyncAccount(context);

        //String authority1 = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, context.getString(R
                            .string.content_authority))
                    .setExtras(new Bundle())
                    .build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    context.getString(R.string.content_authority), new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string
                .sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {
            // Add the account and account type, no password or user data
            // If successful, return the Account object, otherwise report an error.
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            // If you don't set android:syncable="true" in
            // in your <provider> element in the manifest,
            // then call context.setIsSyncable(account, AUTHORITY, 1)
            // here.
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {

        String strInterval = PreferenceManager.getDefaultSharedPreferences(context).getString
                ("sync_frequency",
                        "1");
        int interval = Integer.parseInt(strInterval) * 60;

        // Schedule the sync for periodic execution
        SyncAdapter.configurePeriodicSync(context, interval, interval / 3);

        // Without calling setSyncAutomatically, our periodic sync will not be enabled.
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string
                .content_authority), true);
        //ContentResolver.setSyncAutomatically(newAccount, EventsContract.CONTENT_AUTHORITY, true);

        // Let's do a sync to get things started.
        //syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
