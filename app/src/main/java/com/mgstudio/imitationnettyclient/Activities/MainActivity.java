package com.mgstudio.imitationnettyclient.Activities;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import com.mgstudio.imitationnettyclient.R;
import com.mgstudio.imitationnettyclient.Utils.DebugUtils;
import com.mgstudio.imitationnettyclient.Utils.Appfield;

import timber.log.Timber;

public class MainActivity extends Activity {

    private String tag = this.getClass().getSimpleName();
    private Context mContext = this;


    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

        android.app.ActionBar actionBar = this.getActionBar();
        if (null != actionBar) {
            actionBar.hide();
            DebugUtils.debugLog(tag, "MainActivity---" + "隐藏actionBar");
        //        DebugUtils.debugLog(tag, msg:"MainActivity---" + "隐藏actionBar");
        }

        Timber.plant(new Timber.DebugTree());
    }
}
