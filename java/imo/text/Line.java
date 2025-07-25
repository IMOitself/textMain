package imo.text;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.List;

public class Line {
    List<RectF> charRects = new ArrayList<>();
    List<List<Integer>> wordList = new ArrayList<>(); // each word contains indexes of its chars
    
    String text;
    Integer bottom;
    Integer top;

    Line(String text){
        this.text = text;
    }
    
    boolean isTouched(float touchY){
        return touchY <= bottom && touchY >= top;
    }
}
