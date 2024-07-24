package com.tobell.robot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 途北Tobell
 * @version 1.0.0
 * @date 2024/7/16 上午11:06
 **/
public class RobotThread extends Thread {
    
    private int x;
    private int y;
    private boolean canAuto;
    private int startX;
    private int startY;
    private int limit;
    private String direction;
    private int[][] matrix;
    private Robot robot;
    private MoveThread moveThread;
    private int chestCount = 0;
    
    {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }
    
    public RobotThread(int x, int y, boolean canAuto, int limit, String direction) {
        this.x = x;
        this.y = y;
        this.canAuto = canAuto;
        this.startX = x;
        this.startY = y;
        this.limit = limit;
        this.direction = direction;
    }
    
    @Override
    public void run() {
        do {
            boolean flag = true;
            // 循环执行简单搜索和定式搜素，直到都不命中
            while (flag) {
                flag = clicks();
                flag = flag || form();
            }
            // 如果是全自动模式执行自动开箱、自动猜雷、自动移动
            if (canAuto) {
                // 扫描如果有未开宝箱，自动开箱
                openChest();
                // 猜雷取消
                // 自动移动
                autoMove();
            }
        } while (!interrupted() && canAuto);
    }
    
    /**
     * 简单搜素算法
     * 这里不使用 Box 实例是为了更高的搜索效率
     */
    private boolean clicks() {
        boolean hasClickedInTurn;
        boolean hasClicked = false;
        int clickCount = 0;
        try {
            do {
                hasClickedInTurn = false;
                matrix = BoxRecognizer.getMatrix();
                // 遍历所有数字格
                for (int i = 1; i < matrix.length - 1; i++) {
                    for (int j = 1; j < matrix[0].length - 1; j++) {
                        if (matrix[i][j] >= 1 && matrix[i][j] <= 7) {
                            // 扫描九宫格，获得周围雷数和未知格子数
                            int emptyCount = getSurroundingEmptyCount(j, i);
                            int mineCount = getSurroundingMineCount(j, i);
                            // 如果有必要，点击
                            if (emptyCount != 0){
                                if ((matrix[i][j] == emptyCount + mineCount && i != 1 && j != 1 && i != matrix.length - 2 && j != matrix[0].length - 2) || matrix[i][j] == mineCount) {
                                    robot.mouseMove(20 + 75 * (j - 1), 190 + 75 * (i - 1));
                                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                                    hasClickedInTurn = true;
                                    hasClicked = true;
                                    clickCount++;
                                    sleep(100);
                                    if (clickCount == 11) {
                                        sleep(500);
                                        matrix = BoxRecognizer.getMatrix();
                                        clickCount = 0;
                                    }
                                }
                            }
                        }
                    }
                }
            } while (hasClickedInTurn);
            if (hasClicked) {
                sleep(600);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return hasClicked;
    }
    
    /**
     * 使用定式搜素
     */
    private boolean form() {
        boolean hasClickedInTurn;
        boolean hasClicked = false;
        try {
            do {
                hasClickedInTurn = false;
                matrix = BoxRecognizer.getMatrix();
                // 遍历所有数字格
                for (int i = 2; i < matrix.length - 2; i++) {
                    for (int j = 2; j < matrix[0].length - 2; j++) {
                        if (matrix[i][j] >= 1 && matrix[i][j] <= 7) {
                            // 扫描九宫格，存储该数字格周围的未知格子和雷数
                            Box childBox = new Box(j, i, matrix[i][j]);
                            List<Box> emptyBoxes = getSurroundingEmptyBoxes(childBox);
                            int mineCount = getSurroundingMineCount(j, i);
                            // 如果未知格子不足三个
                            // 遍历未知格周围的九宫格，用集合存储其中的每个数字格
                            // 如果各集合存在另一个公共元素
                            // 说明这个公共元素是原数字格的父格子
                            // 存在父-子关系便可以启动定式
                            if (!emptyBoxes.isEmpty() && emptyBoxes.size() <= 3) {
                                List<List<Box>> tempList = new ArrayList<>();
                                for (Box emptyBox : emptyBoxes) {
                                    tempList.add(getSurroundingNumberBoxes(emptyBox));
                                }
                                // 所有集合对第一个集合取交集，剩余元素保存在第一个集合
                                for (int i1 = 1; i1 < tempList.size(); i1++) {
                                    tempList.getFirst().retainAll(tempList.get(i1));
                                }
                                List<Box> fatherBoxes = tempList.getFirst();
                                // 遍历第一个集合的元素，除了子格子以外别的都是父格子，如果有父格子就开始尝试执行定式
                                // 之前获取过子格子周围的未知格子 List<Box> emptyBoxes 这里可以用
                                fatherBoxes.remove(childBox);
                                if (!fatherBoxes.isEmpty()) {
                                    // 遍历所有可能的父格子
                                    for (Box fatherBox : fatherBoxes) {
                                        // 如果父格子在边缘就算了，防止数组越界
                                        if (fatherBox.x == 1 || fatherBox.y == 1 || fatherBox.x == matrix[i].length - 2 || fatherBox.y == matrix.length - 2) {
                                            continue;
                                        }
                                        // 获取父格子周围的未知格子
                                        List<Box> emptyBoxes1 = getSurroundingEmptyBoxes(fatherBox);
                                        // 对子格子的未知格子取补集，如果补集非空，进一步根据雷数判断能否执行定式
                                        emptyBoxes1.removeAll(emptyBoxes);
                                        int extraEmpty = emptyBoxes1.size();
                                        if (extraEmpty > 0) {
                                            int mineCount1 = getSurroundingMineCount(fatherBox.x, fatherBox.y);
                                            // num1 - mineCount1 == num - mineCount 所有额外格子不是雷，左键
                                            if (fatherBox.count - mineCount1 == childBox.count - mineCount) {
                                                for (Box extraBox : emptyBoxes1) {
                                                    // 保证边缘安全
                                                    if (extraBox.x != 0 && extraBox.y != 0 && extraBox.x != matrix[0].length - 1 && extraBox.y != matrix.length - 1) {
                                                        robot.mouseMove(20 + 75 * (extraBox.x - 1), 190 + 75 * (extraBox.y - 1));
                                                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                                                        hasClickedInTurn = true;
                                                        hasClicked = true;
                                                        sleep(100);
                                                    }
                                                }
                                            }
                                            // extraEmpty == (num1 - mineCount1) - (num - mineCount) 所有额外格子是雷，右键
                                            if (extraEmpty == (fatherBox.count - mineCount1) - (childBox.count - mineCount)) {
                                                for (Box extraBox : emptyBoxes1) {
                                                    if (extraBox.x != 0 && extraBox.y != 0 && extraBox.x != matrix[0].length - 1 && extraBox.y != matrix.length - 1) {
                                                        robot.mouseMove(20 + 75 * (extraBox.x - 1), 190 + 75 * (extraBox.y - 1));
                                                        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                                                        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                                                        hasClickedInTurn = true;
                                                        hasClicked = true;
                                                        sleep(100);
                                                    }
                                                }
                                            }
                                            matrix = BoxRecognizer.getMatrix();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } while (hasClickedInTurn);
            if (hasClicked) {
                sleep(600);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return hasClicked;
    }
    
    public List<Box> getSurroundingEmptyBoxes(Box box) {
        return getSurroundingBoxes(box, 8, 8);
    }
    
    public int getSurroundingEmptyCount(int x1, int y1) {
        return getSurroundingCount(x1, y1, 8, 8);
    }
    
    public List<Box> getSurroundingNumberBoxes(Box box) {
        return getSurroundingBoxes(box, 1, 7);
    }
    
    public int getSurroundingNumberCount(int x1, int y1) {
        return getSurroundingCount(x1, y1, 1, 7);
    }
    
    public List<Box> getSurroundingMineBoxes(Box box) {
        return getSurroundingBoxes(box, 9, 9);
    }
    
    public int getSurroundingMineCount(int x1, int y1) {
        return getSurroundingCount(x1, y1, 9, 9);
    }
    
    private List<Box> getSurroundingBoxes(Box box, int from, int to) {
        List<Box> boxes = new ArrayList<>();
        for (int j = box.x - 1; j < box.x + 2; j++) {
            for (int i = box.y - 1; i < box.y + 2; i++) {
                if (matrix[i][j] >= from && matrix[i][j] <= to) {
                    boxes.add(new Box(j, i, matrix[i][j]));
                }
            }
        }
        return boxes;
    }
    
    private int getSurroundingCount(int x1, int y1, int from, int to) {
        int mineCount = 0;
        for (int j = x1 - 1; j < x1 + 2; j++) {
            for (int i = y1 - 1; i < y1 + 2; i++) {
                if (matrix[i][j] >= from && matrix[i][j] <= to) {
                    mineCount++;
                }
            }
        }
        return mineCount;
    }
    
    public void openChest() {
        try {
            sleep(400);
            matrix = BoxRecognizer.getMatrix();
            for (int i = 1; i < matrix.length - 1; i++) {
                for (int j = 1; j < matrix[0].length - 1; j++) {
                    if (matrix[i][j] == -1) {
                        //System.out.println("open chest " + (x + j) + " " + (y + i));
                        robot.mouseMove(20 + 75 * (j - 1), 190 + 75 * (i - 1));
                        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                        sleep(50);
                        robot.mouseMove(960, 790);
                        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                        sleep(250);
                        //ImageIO.write(BoxRecognizer.getScreen(), "png", new File("C:\\Users\\24674\\Desktop\\chests\\chest" + chestCount + ".png"));
                        chestCount++;
                        robot.mouseMove(960, 630);
                        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                        sleep(100);
                        matrix = BoxRecognizer.getMatrix();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void autoMove() {
        matrix = BoxRecognizer.getMatrix();
        int emptyCount = 0;
        for (int i = 1; i < matrix.length - 1; i++) {
            for (int j = 1; j < matrix[0].length - 1; j++) {
                if (matrix[i][j] == 8) {
                    emptyCount++;
                }
            }
        }
        // 如果全空或全满，需要大幅度移动
        boolean isFullMove = (emptyCount == 0 || emptyCount == (matrix.length - 2) * (matrix[0].length - 2));
        switch (direction) {
            case "right", "1", "+x":
                if (isFullMove) {
                    x += matrix[0].length - 3;
                } else {
                    x += matrix[0].length / 2 + 2;
                }
                break;
            case "left", "2", "-x":
                if (isFullMove) {
                    x -= matrix[0].length - 3;
                } else {
                    x -= matrix[0].length / 2 + 2;
                }
                break;
            case "down", "3", "+y":
                if (isFullMove) {
                    y += matrix.length - 3;
                } else {
                    y += matrix.length / 2;
                }
                if (y > limit) {
                    y = startY;
                    // 开荒模式
                    x += matrix[0].length / 2;
                }
                break;
            case "up", "4", "-y":
                if (isFullMove) {
                    y -= matrix.length - 3;
                } else {
                    y -= matrix.length / 2;
                }
                break;
            default:
        }
        moveThread = new MoveThread(x, y);
        moveThread.start();
        try {
            sleep(600);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
