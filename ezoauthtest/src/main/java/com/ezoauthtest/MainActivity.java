package com.ezoauthtest;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.ezoauth.EZOAuth;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Activity context = this;
        final EZOAuth ezoAuth = new EZOAuth("ADDRESS HERE");
        final String uuid = "hello";
        ezoAuth.setMetaData("hallooooooo");

        ((CheckBox) findViewById(R.id.deviceIdentifier)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ezoAuth.setDeviceIdentifier(uuid);
                } else {
                    ezoAuth.setDeviceIdentifier(null);
                }
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ezoAuth.authenticate(context, ((EditText) findViewById(R.id.editText)).getText().toString(), new EZOAuth.AuthenticateCallback() {
                    @Override
                    public void done(JSONObject data) {
                        Toast.makeText(context, data.toString(), Toast.LENGTH_LONG).show();
                        Log.d("jsonResponse", data.toString());
                    }

                    @Override
                    public void error(String err) {
                        Log.e("error :(", err);
                    }
                });
            }
        });
    }

}
