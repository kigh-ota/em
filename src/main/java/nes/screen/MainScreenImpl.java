package nes.screen;

import javax.swing.*;
import java.awt.*;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class MainScreenImpl implements MainScreen {

    private static final String WINDOW_TITLE = "main";

    private Canvas canvas;

    @Override
    public void init() {
        JFrame mainFrame = new JFrame();
        mainFrame.setTitle(WINDOW_TITLE);
        mainFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        mainFrame.setResizable(false);
        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        mainFrame.getContentPane().add(canvas);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);

        mainFrame.setVisible(true);
    }

    @Override
    public void refresh(MainScreenData data) {
        return;
    }
}
