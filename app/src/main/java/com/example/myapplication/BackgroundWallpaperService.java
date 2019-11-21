package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import static android.hardware.SensorManager.*;

public class BackgroundWallpaperService extends WallpaperService implements SensorEventListener {

    private SensorManager sensorManager;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SENSOR_DELAY_NORMAL);

    }

    @Override
    public Engine onCreateEngine() {
        return new MyWallpaperEngine();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);

        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);

        }
        updateOrientationAngles();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void updateOrientationAngles() {
        getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        getOrientation(rotationMatrix, orientationAngles);
    }

    private double getRoundedDouble(float value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private class MyWallpaperEngine extends Engine {
        private final Handler handler = new Handler();
        private final Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                draw();
            }

        };
        Paint paint = new Paint();
        private boolean visible = true;

        @Override
        public SurfaceHolder getSurfaceHolder() {
            return super.getSurfaceHolder();
        }

        MyWallpaperEngine() {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(BackgroundWallpaperService.this);

            handler.post(drawRunner);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                handler.post(drawRunner);
            } else {
                handler.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            handler.removeCallbacks(drawRunner);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas();

                if (canvas != null) {
                    canvas.save();
                    drawPaint(canvas);
                }
            } finally {
                if (canvas != null)
                    holder.unlockCanvasAndPost(canvas);
            }
            handler.removeCallbacks(drawRunner);
            if (visible) {
                handler.postDelayed(drawRunner, 50);
            }
        }

        private void drawPaint(Canvas canvas) {
            int canvasHeight = canvas.getHeight();

            // set initial color
            canvas.drawColor(Color.BLACK);

            // determine pitch (in degrees) of device
            float pitchDegrees = (float) Math.toDegrees(orientationAngles[1]);

            // determine new y position depending on pitch of device
            float newYPosition = (canvasHeight / 90) * Math.abs(pitchDegrees);

            //if(pitchDegrees<=0 && pitchDegrees>=-90){
            paint.setShader(new LinearGradient(0, 0, 0, (int) newYPosition, 0xFF1b3e62, Color.WHITE, Shader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            //  }

            canvas.drawPaint(paint);

            System.out.println("Y Degree: " + pitchDegrees + " # Y Position: " + newYPosition);
        }

    }
}