package imo.text;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class Editor extends View {
    List<Line> Lines = new ArrayList<>();
    
    Paint mPaint;
    Rect textBounds;
    Rect cursorRect;
    Rect charCursor;
    
    int touchX = 0;
    int touchY = 0;
    
    int lineHeight = -1;
    int lineSpacing = 0;

    int currCursorCharIndex;
    int currCursorLine;


    public Editor(Context context) {
        super(context);
        init();
    }

    public Editor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Editor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init(){
        mPaint = new Paint();
        textBounds = new Rect();
        cursorRect = new Rect();
        
        mPaint.setTextSize(50f);
        mPaint.setColor(Color.WHITE);
    }

    public void setText(String text){
        for(String textLine : text.split("\n")){
            Lines.add(new Line(textLine));
        }
        invalidate(); // will call onDraw()
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (Lines.isEmpty()) return;

        int lastBottom = 0;
        int lineIndex = 0;

        for (Line line : Lines) {
            String lineText = line.text;
            int cumulativeWidth = 0;

            // Initialize line height and spacing (only once)
            if (lineHeight == -1) {
                lineHeight = textBounds.height();
                lineSpacing = lineHeight / 2;
            }

            // Calculate line position
            line.top = lastBottom;
            line.bottom = line.top + lineHeight + lineSpacing;

            if(lineText.isEmpty()) {
                lastBottom = line.bottom;
                lineIndex++;
                continue;
            }

            // Measure text bounds
            mPaint.getTextBounds(lineText, 0, lineText.length(), textBounds);

            // Populate charRightPositions
            if(line.charRightPositions.isEmpty()) {
                for (int i = 0; i < lineText.length(); i++) {
                    float charWidth = mPaint.measureText(lineText, i, i + 1);
                    cumulativeWidth += (int) charWidth;

                    float charRight = cumulativeWidth;
                    int charPosition = i;
                    line.charRightPositions.put(charRight, charPosition);
                }
            }

            if(line.isTouched(touchY)){
                mPaint.setColor(Color.DKGRAY);
                canvas.drawRect(0, line.top, getWidth(), line.bottom, mPaint);

                Float nearestKey = line.charRightPositions.floorKey((float) touchX);
                Float beforeKey = line.charRightPositions.lowerKey(nearestKey);
                float charRight = nearestKey;
                float charLeft = beforeKey + 1; // after the previous charRight

                charCursor.left = (int) charLeft;
                charCursor.right = (int) charRight;
                charCursor.top = line.top;
                charCursor.bottom = line.bottom;
                mPaint.setColor(0xFF888888);
                canvas.drawRect(charCursor, mPaint);
            }

            // Draw text
            drawText(canvas, lineText, line.bottom - lineSpacing);

            // Stop drawing if we're off the bottom of the view
            if (line.bottom > getHeight()) break;

            lastBottom = line.bottom;
            lineIndex++;
        }
    }

    private void drawCursorLine(Canvas canvas, int lineTop, int lineBottom) {
        mPaint.setColor(Color.DKGRAY);
        canvas.drawRect(0, lineTop, getWidth(), lineBottom, mPaint);
    }

    private void drawCursorOnTouch(Canvas canvas, int touchX, String lineText, Rect cursorRect) {
        int overallWidth = 0;
        boolean isCharTouched = false;

        for (int i = 0; i < lineText.length(); i++) {
            float charWidth = mPaint.measureText(lineText, i, i + 1);
            overallWidth += (int) charWidth;

            if (touchX <= overallWidth) {
                cursorRect.left = overallWidth - (int) charWidth;
                cursorRect.right = overallWidth;
                currCursorCharIndex = i;
                isCharTouched = true;
                break;
            }
        }

        if (! isCharTouched) {
            int lastChar = lineText.length() - 1;
            float lastCharWidth = mPaint.measureText(lineText, lastChar, lastChar + 1);
            float charsWidthBefore = mPaint.measureText(lineText, 0, lastChar);
            cursorRect.left = (int) charsWidthBefore;
            cursorRect.right = (int) (charsWidthBefore + lastCharWidth);
            currCursorCharIndex = lineText.length() - 1;
        }

        mPaint.setColor(0xFF888888);
        canvas.drawRect(cursorRect, mPaint);
    }

    private void drawText(Canvas canvas, String line, int y) {
        mPaint.setColor(Color.WHITE);
        canvas.drawText(line, 0, y, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchX = (int) event.getX();
            touchY = (int) event.getY();
            invalidate(); // will call onDraw()
            return true;
        }
        return super.onTouchEvent(event);
    }


    //
    // VIM MACROS
    //
    int moveCursorX = 0;

    public void setMoveCursorX(int amount){
        moveCursorX = amount;
        invalidate(); // will call onDraw() and moveCursorX()
    }

    private void moveCursorX(Canvas canvas, int charIndex, String lineText, Rect cursorRect) {
        float charsWidthBefore = mPaint.measureText(lineText, 0, charIndex);
        float charWidth = mPaint.measureText(lineText, charIndex, charIndex + 1);

        cursorRect.left = (int) charsWidthBefore;
        cursorRect.right = (int) (charsWidthBefore + charWidth);

        mPaint.setColor(0xFF888888);
        canvas.drawRect(cursorRect, mPaint);
    }
}
