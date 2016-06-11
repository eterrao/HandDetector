package com.mmj.handdetectorcalling.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by raomengyang on 6/11/16.
 */
public class CustomVideoView extends View {

    private Bitmap displayCallBackBitmap;

    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (displayCallBackBitmap != null) {
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(displayCallBackBitmap, 320, 568), 0, 0, null);
        }
    }

    public Bitmap getDisplayCallBackBitmap() {
        return displayCallBackBitmap;
    }

    public void setDisplayCallBackBitmap(Bitmap displayCallBackBitmap) {
        this.displayCallBackBitmap = displayCallBackBitmap;
        invalidate();
    }
}
