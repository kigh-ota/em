package nes.screen;

import javax.swing.*;
import java.awt.*;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class MainScreenImpl implements MainScreen {

    private static final String WINDOW_TITLE = "main";

    private Canvas canvas;
    private Graphics gMain;
    private Image buffer;

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
