package imo.text;

import java.util.TreeMap;

public class Line {
    TreeMap<Float, Integer> charRightPositions = new TreeMap<>();
    String text;
    int bottom;
    int top;

    Line(String text){
        this.text = text;
    }
    
    boolean isTouched(int touchY){
        return touchY <= bottom && touchY >= top;
    }


    
}
