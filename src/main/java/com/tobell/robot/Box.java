package com.tobell.robot;

import java.util.Objects;

/**
 * @author 途北Tobell
 * @version 1.0.0
 * @date 2024/7/17 上午11:49
 **/
public class Box {
    
    public int x;
    public int y;
    public int count;
    
    public Box(int x, int y, int count) {
        this.x = x;
        this.y = y;
        this.count = count;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Box box = (Box) o;
        return x == box.x && y == box.y && count == box.count;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y, count);
    }
}
