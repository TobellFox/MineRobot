package com.tobell.robot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 方块处理类
 *
 * @author 途北Tobell
 * @version 1.0.0
 * @date 2024/7/16 上午8:24
 **/
public class BoxRecognizer {
    
    static final Color BORDER_TOP = new Color(255, 255, 255);
    static final Color BORDER_BOTTOM = new Color(100, 100, 100);
    
    static final Color CONTENT_EMPTY = new Color(169, 169, 169);
    static final Color CONTENT_BOOM = new Color(0, 0, 0);
    static final Color CHEST_OPEN = new Color(77, 53, 38);
    
    static final Color CONTENT_ZERO = new Color(240, 240, 240);
    static final Color CONTENT_ONE = new Color(86, 105, 221);
    static final Color CONTENT_TWO = new Color(14, 166, 64);
    static final Color CONTENT_THREE = new Color(239, 52, 56);
    static final Color CONTENT_FOUR = new Color(57, 51, 167);
    static final Color CONTENT_FIVE = new Color(159, 49, 47);
    static final Color CONTENT_SIX = new Color(16, 133, 130);
    static final Color CONTENT_SEVEN = new Color(51, 50, 50);
    
    static Robot robot;
    
    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 0 ~ 7 相应数字
     * 8 未知空盒子
     * 9 雷、旗子
     * 10 外圈填充
     * -1 宝箱
     */
    public static int getKind(Color colorBorder, Color colorContent, Color colorContent2, Color colorContent3) {
        if (colorBorder.equals(BORDER_TOP) || colorBorder.equals(BORDER_BOTTOM) || (colorContent.equals(CONTENT_BOOM) && colorContent2.equals(CONTENT_BOOM) && colorContent3.equals(CONTENT_BOOM))) {
            if (colorContent.equals(CONTENT_EMPTY) && colorContent2.equals(CONTENT_EMPTY) && colorContent3.equals(CONTENT_EMPTY)) {
                return 8;
            } else {
                return 9;
            }
        }
        if (colorContent.equals(CONTENT_ONE)) {
            return 1;
        }
        if (colorContent.equals(CONTENT_TWO) || colorContent2.equals(CONTENT_TWO)) {
            return 2;
        }
        if (colorContent.equals(CONTENT_THREE)) {
            return 3;
        }
        if (colorContent.equals(CONTENT_FOUR) || colorContent2.equals(CONTENT_FOUR)) {
            return 4;
        }
        if (colorContent.equals(CONTENT_FIVE)) {
            return 5;
        }
        if (colorContent.equals(CONTENT_SIX) || colorContent2.equals(CONTENT_SIX)) {
            return 6;
        }
        if (colorContent.equals(CONTENT_SEVEN) || colorContent2.equals(CONTENT_SEVEN)) {
            return 7;
        }
        if (colorContent.equals(CONTENT_ZERO) || colorContent.equals(CHEST_OPEN)) {
            return 0;
        }
        return -1;
    }
    
    /**
     * 抓取当前屏幕信息（截屏）
     */
    public static BufferedImage getScreen() {
        Rectangle rect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        return robot.createScreenCapture(rect);
    }
    
    /**
     * 图像各个采样点 RGB 采样，拿到图像识别结果
     */
    public static int[][] getMatrix(BufferedImage img) {
        img = ifBoomed(img);
        int[][] matrix = new int[14][28];
        int y = 157;
        for (int xBox = 0; xBox < 28; xBox++) {
            matrix[0][xBox] = 10;
            matrix[13][xBox] = 10;
        }
        for (int yBox = 1; yBox < 13; yBox++) {
            matrix[yBox][0] = 10;
            matrix[yBox][27] = 10;
            int x = 25;
            for (int xBox = 1; xBox < 27; xBox++) {
                if (yBox == 1) {
                    matrix[1][xBox] = getKind(new Color(img.getRGB(x, 225)), new Color(img.getRGB(x, 191)), new Color(img.getRGB(x + 5, 191)), new Color(img.getRGB(x - 5, 186)));
                } else {
                    matrix[yBox][xBox] = getKind(new Color(img.getRGB(x, y)), new Color(img.getRGB(x, y + 34)), new Color(img.getRGB(x + 5, y + 34)), new Color(img.getRGB(x - 5, y + 29)));
                }
                x += 75;
                if ((xBox & 3) == 1) {
                    x--;
                }
            }
            y += 75;
            if ((yBox & 3) == 3) {
                y--;
            }
        }
        return matrix;
    }
    
    /**
     * 从现有文件得到识别矩阵
     */
    private static int[][] getMatrix(String filePath) {
        BufferedImage img;
        try {
            img = ImageIO.read(new FileInputStream(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return getMatrix(img);
    }
    
    /**
     * 从当前屏幕得到识别矩阵
     */
    public static int[][] getMatrix() {
        return getMatrix(getScreen());
    }
    
    /**
     * 如果踩炸，自动复活
     */
    public static BufferedImage ifBoomed(BufferedImage img) {
        if (new Color(img.getRGB(500, 155)).equals(new Color(28, 54, 58))) {
            try {
                robot.mouseMove(960, 640);
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                Thread.sleep(100);
                if (new Color(getScreen().getRGB(500, 155)).equals(new Color(28, 54, 58))){
                    Thread.sleep(5900);
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            img = getScreen();
        }
        return img;
    }
}
