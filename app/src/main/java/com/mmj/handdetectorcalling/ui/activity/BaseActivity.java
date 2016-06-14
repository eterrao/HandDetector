package com.mmj.handdetectorcalling.ui.activity;

import android.app.Activity;
import android.os.Bundle;

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
