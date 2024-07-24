package com.tobell.robot;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * @author 途北Tobell
 * @version 1.0.0
 * @date 2024/7/16 下午8:50
 **/
public class MoveThread extends Thread {
    private int x;
    private int y;
    
    public MoveThread(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public void run() {
        try {
            Robot robot = new Robot();
            robot.mouseMove(200, 100);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseMove(742, 905);
            insert(robot, x);
            robot.mouseMove(874, 904);
            insert(robot, y);
            robot.mouseMove(850, 980);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        } catch (AWTException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void insert(Robot robot, int num) throws InterruptedException {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        sleep(20);
        robot.keyPress(num / 1000 + 96);
        robot.keyRelease(num / 1000 + 96);
        sleep(2);
        robot.keyPress(num / 100 % 10 + 96);
        robot.keyRelease(num / 100 % 10 + 96);
        sleep(2);
        robot.keyPress(num / 10 % 10 + 96);
        robot.keyRelease(num / 10 % 10 + 96);
        sleep(5);
        robot.keyPress(num % 10 + 96);
        robot.keyRelease(num % 10 + 96);
    }
}
