package com.tobell.robot;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

import java.awt.event.KeyEvent;

/**
 * @author 途北Tobell
 * @version 1.0.0
 * @date 2024/7/16 下午8:33
 **/
public class MineRobot {
    
    public int x;
    public int y;
    public int limit;
    public String direction;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int UP = 3;
    public static final int DOWN = 4;
    public static final int SPACE = 5;
    public static final int ZERO = 6;
    public static final int E = 7;
    public static final int K = 8;
    RobotThread robotThread;
    MoveThread moveThread;
    
    public MineRobot(int x, int y, int limit, String direction) {
        this.x = x;
        this.y = y;
        this.limit = limit;
        this.direction = direction;
        init();
    }
    
    private void auto() {
        if (robotThread == null || !robotThread.isAlive()) {
            robotThread = new RobotThread(x, y, true, limit, direction);
            robotThread.start();
        }
    }
    
    private void stop() {
        robotThread.interrupt();
        robotThread = null;
    }
    
    private void step() {
        if (robotThread == null || !robotThread.isAlive()) {
            robotThread = new RobotThread(x, y, false, limit, direction);
            robotThread.start();
        }
    }
    
    /**
     * 初始化键盘监听
     */
    private void init() {
        JIntellitype.getInstance().registerHotKey(LEFT, 0, 'A');
        JIntellitype.getInstance().registerHotKey(RIGHT, 0, 'D');
        JIntellitype.getInstance().registerHotKey(UP, 0, 'W');
        JIntellitype.getInstance().registerHotKey(DOWN, 0, 'S');
        JIntellitype.getInstance().registerHotKey(SPACE, 0, KeyEvent.VK_SPACE);
        JIntellitype.getInstance().registerHotKey(ZERO, 0, '0');
        JIntellitype.getInstance().registerHotKey(E, 0, 'E');
        JIntellitype.getInstance().registerHotKey(K, 0, 'K');
        HotkeyListener hotkeyListener = code -> {
            if (moveThread == null || !moveThread.isAlive()) {
                switch (code) {
                    case SPACE -> step();
                    case ZERO -> stop();
                    case K -> auto();
                    case E -> System.exit(0);
                    default -> {
                        if (robotThread == null || !robotThread.isAlive()) {
                            switch (code) {
                                case LEFT -> x -= 5;
                                case RIGHT -> x += 5;
                                case UP -> y -= 3;
                                case DOWN -> y += 3;
                                default -> {
                                }
                            }
                            moveThread = new MoveThread(x, y);
                            moveThread.start();
                        }
                    }
                }
            }
        };
        JIntellitype.getInstance().addHotKeyListener(hotkeyListener);
        System.out.println("正在监听...");
    }
}
