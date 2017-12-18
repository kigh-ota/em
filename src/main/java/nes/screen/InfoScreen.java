package nes.screen;

import java.awt.*;

public class InfoScreen extends SwingScreen {
    public static final int WIDTH = 8 * 16 * 2;
    public static final int HEIGHT = 8 * 16 + 8 * 4;

    public void init(MainScreen mainScreen) {
        super.init("info", WIDTH, HEIGHT);
        Point location = mainScreen.mainFrame.getLocation();
        location.translate(0, mainScreen.mainFrame.getHeight());
        mainFrame.setLocation(location);
    }

    public void refresh(ScreenData data) {
        gBuffer.clearRect(0, 0, WIDTH, HEIGHT);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                gBuffer.setColor(data.get(x, y));
                gBuffer.drawLine(x, y, x, y);
            }
        }
        gMain.drawImage(buffer, 0, 0, canvas);
    }
}
