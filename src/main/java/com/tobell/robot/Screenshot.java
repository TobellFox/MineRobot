package com.tobell.robot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * @author 途北Tobell
 * @version 1.0.0
 * @date 2024/7/19 下午8:09
 **/
public class Screenshot {
    public static void main(String[] args) throws IOException {
        ImageIO.write(BoxRecognizer.getScreen(), "png", new File("C:\\Users\\Administrator\\Desktop\\MineBot\\src\\test.png"));
    }
}
