package com.sivag1.juiceboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sivag1.juiceboard.data.JuiceLevel;
import com.sivag1.juiceboard.data.MyCursorAdapter;
import com.sivag1.juiceboard.data.MyProvider;
import com.sivag1.juiceboard.sync.SyncAdapter;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity 
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private List<JuiceLevel> juiceLevelList = new ArrayList<JuiceLevel>();
    ListView listView;
    //DisplayAdapter dp;
    boolean isCurrentDeviceAdded = false;
    private MyCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Adding Toolbar to Main screen
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getString("sync_frequency", null) == null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("sync_frequency", "15");
            editor.commit();
        }
        SyncAdapter.initializeSyncAdapter(this);

        mAdapter = new MyCursorAdapter(this, null, 0);

        //dp = new DisplayAdapter(this, R.layout.listview_item_row, juiceLevelList);
        listView = (ListView) findViewById(android.R.id.list);
        TextView emptyText = (TextView) findViewById(android.R.id.empty);
        listView.setEmptyView(emptyText);
        //listView.setAdapter(dp);
        listView.setAdapter(mAdapter);
        getSupportLoaderManager().initLoader(0, null, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        if (isCurrentDeviceAdded) {
            MenuItem item = menu.findItem(R.id.action_add);
            item.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            showSettings();
            return true;
        } else if (id == R.id.action_add) {
            showActivateMe();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, 0);
    }

    private void showActivateMe() {
        Intent intent = new Intent(this, ActivateMeActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if (resultCode == LoginActivity.RESULT_DISCONNECT || resultCode == LoginActivity
                    .RESULT_LOGOFF) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("RESULT", resultCode);
                startActivity(intent);
                this.finish();
            }
        }
    }

    @Override
    protected void onStart() {
        BackendDataFacade.update(this, false, null);

        juiceLevelList.clear();
        //BackendDataFacade.fetchJuiceLevels(this, juiceLevelList);
        //dp.notifyDataSetChanged();

        String deviceId = Utility.getDeviceId(this);

        for (JuiceLevel juiceLevel : juiceLevelList) {
            if (deviceId != null && deviceId.equals(juiceLevel.getDeviceId())) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                        (this).edit();
                editor.putString("objectId", juiceLevel.getObjectId());
                editor.putString("deviceName", juiceLevel.getDeviceName());
                editor.commit();
                isCurrentDeviceAdded = true;
                break;
            }
        }

        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(0, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by date.
        String sortOrder = "deviceName" + " ASC";

        return new CursorLoader(
                this,
                MyProvider.JUICELEVELS_URI,
                null,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
