package pl.piechnat.skycashticket;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

public class MyImageView extends AppCompatImageView {

    class Circle {
        float x, y, MAX_SIZE;
        int size = 0;
    }
    Circle circle = new Circle();
    Paint paint1 = new Paint();
    Paint paint2 = new Paint();
    int animPhase = 0, animStep, tmpColor, phaseSize;

    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        circle.MAX_SIZE = (float)(metrics.widthPixels / 2.8); // * metrics.density
        paint1.setStyle(Paint.Style.FILL);
        paint1.setAntiAlias(true);
        paint1.setColor(0x99FFFFFF);
        paint2.setStyle(Paint.Style.FILL);
        paint2.setAntiAlias(true);
        paint2.setColor(0x99C8C8C8);
        animStep = (int)(circle.MAX_SIZE / 6);
        phaseSize = (int)(circle.MAX_SIZE * 0.5);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (animPhase > 0) {
            canvas.drawCircle(circle.x, circle.y, circle.size, paint1);
            canvas.drawCircle(circle.x, circle.y, circle.size - 100, paint2);
        }
    }

    void animStart() {
        invalidate();
        switch (animPhase) {
            case 1:
                if (circle.size < phaseSize) circle.size += animStep; else animPhase = 2;
                break;
            case 2:
                if (circle.size < circle.MAX_SIZE) circle.size += animStep; else animPhase = 3;
            case 3:
                int alpha = paint1.getAlpha() - 5;
                if (alpha > 0) {
                    paint1.setAlpha(alpha);
                    paint2.setAlpha(alpha);
                } else {
                    alpha = 0x99;
                    circle.size = 0;
                    paint1.setAlpha(alpha);
                    paint2.setAlpha(alpha);
                    animPhase = 0;
                    invalidate();
                    return;
                }
                break;
            default: return;
        }
        postOnAnimation(new Runnable() {
            public void run() { animStart(); }
        } );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            circle.x = event.getX();
            circle.y = event.getY();
            animPhase = 1;
            animStart();
        }
        return true;
    }
}
