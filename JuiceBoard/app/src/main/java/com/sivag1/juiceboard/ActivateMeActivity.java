package com.sivag1.juiceboard;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class ActivateMeActivity extends Activity {

    TextView txtModel;
    EditText txtName;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    String objectId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate_me);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        objectId = sp.getString("objectId", null);
        editor = sp.edit();

        txtModel = (TextView) findViewById(R.id.device_id);
        txtModel.setText(Utility.getDeviceName());

        txtName = (EditText) findViewById(R.id.device_name_edit);
        String deviceName = sp.getString("deviceName", null);
        if (deviceName != null)
            txtName.setText(deviceName);
    }

    public void doActivate(View v) {
        if (txtName.getText() == null || txtName.getText().toString() == null
                || txtName.getText().toString().length() == 0) {
            editor.putString("deviceName", getResources().getString(R.string.hint));
        } else {
            editor.putString("deviceName", txtName.getText().toString());
        }
        editor.commit();
        BackendDataFacade.update(this, true, null);
        Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
        finish();
    }


}
