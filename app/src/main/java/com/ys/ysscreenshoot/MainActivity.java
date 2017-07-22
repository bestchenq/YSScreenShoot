package com.ys.ysscreenshoot;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    class GlobalScreenshot {
        private WindowManager mWindowManager;
        private Display mDisplay;
        private DisplayMetrics mDisplayMetrics;
        private Matrix mDisplayMatrix;
        private Bitmap mScreenBitmap;

        /**
         * @param context everything needs a context :(
         */
        public GlobalScreenshot(Context context) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            // Inflate the screenshot layout
            mDisplayMatrix = new Matrix();
            // Setup the window that we are going to use
            mDisplay = mWindowManager.getDefaultDisplay();
            mDisplayMetrics = new DisplayMetrics();
            mDisplay.getRealMetrics(mDisplayMetrics);
        }

        /**
         * @return the current display rotation in degrees
         */
        private float getDegreesForRotation(int value) {
            switch (value) {
                case Surface.ROTATION_90:
                    return 360f - 90f;
                case Surface.ROTATION_180:
                    return 360f - 180f;
                case Surface.ROTATION_270:
                    return 360f - 270f;
            }
            return 0f;
        }

        /**
         * Takes a screenshot of the current display and shows an animation.
         */
        void takeScreenshot(String path) {
            // We need to orient the screenshot correctly (and the Surface api seems to take screenshots
            // only in the natural orientation of the device :!)
            mDisplay.getRealMetrics(mDisplayMetrics);
            float[] dims = {mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels};
            float degrees = getDegreesForRotation(mDisplay.getRotation());
            boolean requiresRotation = (degrees > 0);
            if (requiresRotation) {
                // Get the dimensions of the device in its native orientation
                mDisplayMatrix.reset();
                mDisplayMatrix.preRotate(-degrees);
                mDisplayMatrix.mapPoints(dims);
                dims[0] = Math.abs(dims[0]);
                dims[1] = Math.abs(dims[1]);
            }
            Log.d("tests", "takeScreenshot, dims, w-h: " + dims[0] + "-" + dims[1] + "; dm w-h: " + mDisplayMetrics.widthPixels + mDisplayMetrics.heightPixels);
            // Take the screenshot
            mScreenBitmap = SurfaceControl.screenshot((int) dims[0], (int) dims[1]);
            if (mScreenBitmap == null) {
                //notifyScreenshotError(mContext, mNotificationManager);
                //  finisher.run();
                return;
            }
            saveBitmapToGallery(mScreenBitmap, path);
        }

        private void saveBitmapToGallery(Bitmap bitmap, String path) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream out;
            try {
                out = new FileOutputStream(file);
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    out.flush();
                    out.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
