package com.mmj.handdetectorcalling.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Environment;
import android.view.Surface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by raomengyang on 6/5/16.
 */
public class CustomCameraUtils {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    // 设置相机屏幕显示方向（注意仅仅是屏幕的显示方向改变，摄像头采集的图像方向并未改变）
    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int displayDegree = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayDegree = (info.orientation + degrees) % 360;
            displayDegree = (360 - displayDegree) % 360;  // compensate the mirror
        } else {
            // back-facing
            displayDegree = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(displayDegree);
    }

    /**
     * 获取当前屏幕旋转角度
     *
     * @param activity
     * @return 0表示是竖屏; 90表示是左横屏; 180表示是反向竖屏; 270表示是右横屏
     */
    public static int getDisplayRotation(Activity activity) {
        if (activity == null)
            return 0;
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }


    // save image to sdcard path: Pictures/MyTestImage/
    public static void saveImageDataToStorage(byte[] imageData) {
        File imageFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (imageFile == null) {
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(imageData);
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            SystUtils.o("File not found: " + e.getMessage());

        } catch (IOException e) {
            e.printStackTrace();
            SystUtils.o("Error accessing file: " + e.getMessage());
        }
    }

    public static File getOutputMediaFile(int type) {
        File imageFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyTestImage");

        if (!imageFileDir.exists()) {
            if (!imageFileDir.mkdirs()) {
                SystUtils.o("can't makedir for imagefile");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File imageFile;
        if (type == MEDIA_TYPE_IMAGE) {
            imageFile = new File(imageFileDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            imageFile = new File(imageFileDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }
        return imageFile;
    }


    public static String saveBitmap2FileUseQuality(Bitmap bitmap, int quality) {
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        File imageFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyTestImage");
        if (!imageFileDir.exists()) {
            if (!imageFileDir.mkdirs()) {
                SystUtils.o("can't makedir for imagefile");
                return null;
            }
        }
        String path = imageFileDir.getPath() + CustomFileUtils.getImageFileName();
        OutputStream stream = null;
        try {
            if (quality <= 0 || quality > 100) quality = 100;
            stream = new FileOutputStream(path);
            bitmap.compress(format, quality, stream);
            stream.flush();
//            if (null != bitmap && !bitmap.isRecycled()) {
//                bitmap.recycle();
//            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != stream) stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path;
    }
}
