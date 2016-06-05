package com.mmj.handdetectorcalling.ui.activity;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by raomengyang on 6/5/16.
 */
public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        setListeners();
        initData();
    }

    protected abstract void initViews();

    protected abstract void setListeners();

    protected void initData() {
    }


}
