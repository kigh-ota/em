package nes.screen;

import javax.swing.*;
import java.awt.*;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public abstract class SwingScreen {
    protected Canvas canvas;
    protected Graphics gMain;
    protected Graphics gBuffer;
    protected Image buffer;

    private int width;
    private int height;

    public void init(String title, int width, int height) {
        this.width = width;
        this.height = height;

        JFrame mainFrame = new JFrame();
        mainFrame.setTitle(title);
        mainFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        mainFrame.setResizable(false);
        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(width, height));

        mainFrame.getContentPane().add(canvas);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);

        mainFrame.setVisible(true);

        gMain = canvas.getGraphics();
        buffer = canvas.createImage(width, height);
        gBuffer = buffer.getGraphics();
    }
}
