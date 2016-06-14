package com.mmj.handdetectorcalling.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.view.View;

import com.mmj.handdetectorcalling.application.CustomApplication;
import com.mmj.handdetectorcalling.utils.ScreenUtils;

/**
 * 自定义VideoView
 * 实现对方视频通话回显
 */
public class CustomVideoView extends View {

    public static int DISPLAY_WIDTH = ScreenUtils.getScreenWidth(CustomApplication.getContext()) / 4; // 回显的宽、高
    public static int DISPLAY_HEIGHT = ScreenUtils.getScreenHeight(CustomApplication.getContext()) / 4;

    private Bitmap displayCallBackBitmap; // 以Bitmap传入当前View，并通过View的onDraw方法绘制对方视频的每一帧

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
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(displayCallBackBitmap, DISPLAY_WIDTH, DISPLAY_HEIGHT), 0, 0, null);
        }
    }

    public Bitmap getDisplayCallBackBitmap() {
        return displayCallBackBitmap;
    }

    public void setDisplayCallBackBitmap(Bitmap displayCallBackBitmap) {
        this.displayCallBackBitmap = displayCallBackBitmap;
        invalidate();
    }

    public static int getDisplayWidth() {
        return DISPLAY_WIDTH;
    }

    public static void setDisplayWidth(int displayWidth) {
        DISPLAY_WIDTH = displayWidth;
    }

    public static int getDisplayHeight() {
        return DISPLAY_HEIGHT;
    }

    public static void setDisplayHeight(int displayHeight) {
        DISPLAY_HEIGHT = displayHeight;
    }
}
