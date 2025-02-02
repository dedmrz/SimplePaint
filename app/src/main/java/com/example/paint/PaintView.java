package com.example.paint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PaintView extends View {

    //Задание переменных

    public static int BRUSH_SIZE = 10;
    public static final int DEFAULT_COLOR = Color.RED;
    public static final int DEFAULT_BG_COLOR = Color.WHITE;
    private static final float TOUCH_TOLERANCE = 4;

    private float mX, mY;
    private Path mPath;
    private Paint mPaint;
    private int currentColor;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int strokeWidth;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private DateFormat dateFormat;

    private ArrayList<Draw> paths = new ArrayList<>();
    private ArrayList<Draw> undo = new ArrayList<>();


    public PaintView(Context context) {

        super(context, null);

    }


    public PaintView(Context context, AttributeSet attrs) {

        super(context, attrs);
//описание кисти
        mPaint = new Paint();
        mPaint.setAntiAlias(true); //обеспечение сглаживания диагональных линий
        mPaint.setDither(true); // изменяет свои цвета при рисовании на устройстве с менее чем 8 битами.
        mPaint.setColor(DEFAULT_COLOR); // задание цвета
        mPaint.setStyle(Paint.Style.STROKE); //задание стиля, для ровности линии
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND); //круглый конец линии
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);

    }

    public void initialise (DisplayMetrics displayMetrics) {

        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;

    }

    @Override

    protected void onDraw(Canvas canvas) {

        canvas.save();
        mCanvas.drawColor(backgroundColor); // WRONG

        for (Draw draw : paths) {

            mPaint.setColor(draw.color); // WRONG
            mPaint.setStrokeWidth(draw.strokeWidth);
            mPaint.setMaskFilter(null);

            mCanvas.drawPath(draw.path, mPaint);

        }

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();

    }

    private void touchStart (float x, float y) {

        mPath = new Path();

        Draw draw = new Draw(currentColor, strokeWidth, mPath);
        paths.add(draw);

        mPath.reset();
        mPath.moveTo(x, y);

        mX = x;
        mY = y;

    }

    private void touchMove (float x, float y) {

        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {

            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);

            mX = x;
            mY = y;

        }

    }

    private void touchUp () {

        mPath.lineTo(mX, mY);

    }

    //Слушалка нажатия для рисования
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);

                invalidate();
                break;

        }

        return true;

    }

    //Очистка полотна
    public void clear () {

        backgroundColor = DEFAULT_BG_COLOR;

        paths.clear();
        invalidate();

    }

    //Отмена последнего шага
    public void undo () {

        if (paths.size() > 0) {

            undo.add(paths.remove(paths.size() - 1));
            invalidate(); // add

        } else {

            Toast.makeText(getContext(), "Нечего возвращать", Toast.LENGTH_LONG).show(); //Если не было действий ввыводит сообщение об этом

        }

    }

    //Отмена изменения
    public void redo () {

        if (undo.size() > 0) {

            paths.add(undo.remove(undo.size() - 1));
            invalidate(); // add

        } else {

            Toast.makeText(getContext(), "Нечего возвращатьs", Toast.LENGTH_LONG).show();

        }

    }
    //Выбор толщины линии
    public void setStrokeWidth (int width) {

        strokeWidth = width;

    }

    //Выбор цвета
    public void setColor (int color) {

        currentColor = color;

    }

    public void saveImage () {
        //Выбор директории для загрузки изображения
        File sdDirectory = Environment.getExternalStorageDirectory();
        File subDirectory = new File(sdDirectory.toString() + "/DCIM/Camera/");

        //Если выбранная директория существует, то генерируется имя файла
        if (subDirectory.exists()) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmm");
            Date todayDate = new Date();
            String name = dateFormat.format(todayDate);
            File image = new File(subDirectory, "/image" + name + ".png");
            FileOutputStream fileOutputStream;

            //Попытка сохранения
            try {

                fileOutputStream = new FileOutputStream(image);

                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

                fileOutputStream.flush();
                fileOutputStream.close();

                Toast.makeText(getContext(), "Сохранено", Toast.LENGTH_LONG).show();

            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            }
        }
    }
}