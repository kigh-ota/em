package nes.screen;

import lombok.extern.slf4j.Slf4j;
import nes.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

@Slf4j
public class MainScreenImpl implements MainScreen {

    private static final String WINDOW_TITLE = "main";

    private Canvas canvas;
    private Graphics gMain;
    private Image buffer;

    private Controller controller;

    public MainScreenImpl(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void init() {
        JFrame mainFrame = new JFrame();
        mainFrame.setTitle(WINDOW_TITLE);
        mainFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        mainFrame.setResizable(false);
        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        canvas.addKeyListener(controller);

        mainFrame.getContentPane().add(canvas);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);

        mainFrame.setVisible(true);

        gMain = canvas.getGraphics();
        buffer = canvas.createImage(WIDTH, HEIGHT);
    }

    @Override
    public void refresh(MainScreenData data) {
        Graphics g = buffer.getGraphics();
        g.clearRect(0, 0, WIDTH, HEIGHT);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                g.setColor(data.get(x, y));
                g.drawLine(x, y, x, y);
            }
        }

        gMain.drawImage(buffer, 0, 0, canvas);
    }
}
