package ece.ece477.grp4.bb8_ble_remote;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Chris on 11/30/2016.
 */
public class AnalogStickView extends View {
    // Shamelessly borrowed from http://stackoverflow.com/questions/18661107/android-creating-an-analogue-type-animated-image-for-movement

    OnMoveListener moveListener;
    Paint grey = new Paint();
    Paint touchPaint = new Paint();
    float x, y;
    int w, h;
    int motorL, motorR;
    boolean clearPosition;
    static final double pi = 3.14159265358979323;

    public AnalogStickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        grey.setColor(Color.GRAY);
        grey.setStyle(Paint.Style.STROKE);
        touchPaint.setColor(Color.parseColor("#FF8800"));
        touchPaint.setAlpha(128);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Activity parentActivity = (Activity) getContext();
        if (parentActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int prevw, int prevh) {
        this.w = w;
        this.h = h;
        x = w/2;
        y = h/2;
        super.onSizeChanged(w, h, prevw, prevh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw lines and circles
        drawView(canvas);
        if (clearPosition) {
            x = 0;
            y = 0;
            motorL = 0;
            motorR = 0;
            invalidate();
            clearPosition = false;
        }
    }

    private void drawView(final Canvas canvas) {
        // center lines
        canvas.drawLine((float)(w/2.0),(float)(h),(float)(w/2.0),(float)0,grey);
        canvas.drawLine((float)(w),(float)(h/2),(float)(0),(float)(h/2),grey);
        // center circles, 25, 50, 75, 100%
        canvas.drawCircle((float)(w/2.0),(float)(h/2.0),(float)(w*0.125),grey);
        canvas.drawCircle((float)(w/2.0),(float)(h/2.0),(float)(w*0.25),grey);
        canvas.drawCircle((float)(w/2.0),(float)(h/2.0),(float)(w*0.375),grey);
        canvas.drawCircle((float)(w/2.0),(float)(h/2.0),(float)(w*0.50),grey);
        // touchCircle
        canvas.drawCircle(x,y,(float)(w/10.0),touchPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchPaint.setAlpha(128);
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                // update position,
                updatePosition(event);
                // show touch dot
                break;
            case MotionEvent.ACTION_MOVE :
                // update position,
                updatePosition(event);
                // move dot
                break;
            case MotionEvent.ACTION_UP :
                // hide dot
                center();
                touchPaint.setAlpha(0);
                // send 0,0
                clearPosition = true;
                break;
            default : break;
        }

        return true;
    }

    private void center() {
        x = (float)(w/2.0);
        y = (float)(h/2.0);
        moveListener.onMovement(0,0);
        invalidate();
    }

    void updatePosition(MotionEvent e) {
        x = e.getX();
        y = e.getY();
        /*
        if(x > w) {
            x = w;
        }
        if(y > h) {
            y = h;
        }
        if(x < 0) {
            x = 0;
        }
        if(y < 0) {
            y = 0;
        }*/
        // edge clipping conditions yay
        // this is going to be done in the absolute coordinate plane because consistency is sad
        double theta = Math.atan2((y-(h/2)),(x-(w/2)));
        if (getMagnitude() > w/2.0) {
            // if x is past the center then we want cosine to be positive, so c+cosine of positive
            // if x is < center then cosine is negative, so c-cosine(positive)
            //x = x > w/2 ? (float)(w/2.0)+(float)((w/2.0)*Math.cos(theta)) : (float)(w/2.0)-(float)((w/2.0)*Math.cos(theta));
            //y = y > h/2 ? (float)(h/2.0)+(float)((h/2.0)*Math.sin(theta)) : (float)(h/2.0)-(float)((h/2.0)*Math.sin(theta));
            x = (float)(w/2.0)+(float)((w/2.0)*Math.cos(theta));
            y = (float)(h/2.0)+(float)((h/2.0)*Math.sin(theta));
        }
        theta = Math.atan2((y-(h/2)),-(x-(w/2)));
        // set motor powers based on x and y
        double scaledPower = (getMagnitude()/(w/2.0))*100;
        motorL = (int)Math.round((float)scaledPower * Math.cos(theta+3*pi/4.0));
        motorR = (int)Math.round((float)scaledPower * Math.sin(theta+3*pi/4.0));

        moveListener.onMovement(motorL,motorR);
        invalidate();
    }

    double getMagnitude() {
        double magnitude = 0;
        //double circleRadius = w/2.0;
        magnitude = Math.pow((Math.pow((double)(x-(h/2)),2.0))+(Math.pow((double)(y-(w/2)),2.0)),0.5);
        return magnitude;
    }

    public void setOnMoveListener(OnMoveListener listener) {
        moveListener = listener;
    }

    public interface OnMoveListener {
        public void onMovement(int motorL, int motorR);
    }
}
