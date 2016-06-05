package com.mmj.handdetectorcalling.ui.activity;

import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.mmj.handdetectorcalling.R;
import com.mmj.handdetectorcalling.utils.SystUtils;

/**
 * Created by raomengyang on 6/5/16.
 */
public class PhotoPreviewActivity extends BaseActivity {

    private ImageView photoPreviewIV;

    @Override
    protected void initViews() {
        setContentView(R.layout.activity_photo_preview);
        photoPreviewIV = (ImageView) findViewById(R.id.iv_photo_preview);
    }

    @Override
    protected void setListeners() {

    }

    @Override
    protected void initData() {
        super.initData();
        photoPreviewIV.setImageBitmap(BitmapFactory.decodeFile(SystUtils.capturePath));
    }
}
