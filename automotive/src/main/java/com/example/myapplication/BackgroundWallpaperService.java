package com.example.myapplication;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class BackgroundWallpaperService extends WallpaperService  {


    @Override
    public Engine onCreateEngine() {
        return new MyWallpaperEngine();
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

        private boolean visible = true;

        @Override
        public SurfaceHolder getSurfaceHolder() {
            return super.getSurfaceHolder();
        }

        MyWallpaperEngine() {
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
                    drawOnCanvas(canvas);
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

        // at the moment no animated elements. just two bars.
        private void drawOnCanvas(Canvas canvas) {
            // paint and draw background
            canvas.drawColor(Color.GRAY);

            // paint and draw bar 1
            Path pathBar1 = new Path();
            pathBar1.moveTo(0,0);
            pathBar1.lineTo(getDesiredMinimumWidth()/4,0);
            pathBar1.lineTo(getDesiredMinimumWidth()/2,getDesiredMinimumHeight());
            pathBar1.lineTo(0,getDesiredMinimumHeight());
            pathBar1.close();

            Paint paintingBar1 = new Paint();
            paintingBar1.setColor(Color.parseColor("#0c3868"));
            paintingBar1.setShadowLayer(5, 0, 0, Color.BLACK);

            canvas.drawPath(pathBar1, paintingBar1);

            // paint and draw bar 2
            Paint paintingBar2 = new Paint();
            paintingBar2.setColor(Color.parseColor("#0e4a80"));
            paintingBar2.setShadowLayer(5, 0, 0, Color.BLACK);

            Path pathBar2 = new Path();
            pathBar2.moveTo(getDesiredMinimumWidth(),0);
            pathBar2.lineTo(getDesiredMinimumWidth()/4 + 75,0);
            pathBar2.lineTo(getDesiredMinimumWidth()/2 + 75,getDesiredMinimumHeight());
            pathBar2.lineTo(getDesiredMinimumWidth(),getDesiredMinimumHeight());
            pathBar2.close();

            canvas.drawPath(pathBar2, paintingBar2);

        }

    }
}