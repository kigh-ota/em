package nes.screen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nes.Controller;

@Slf4j
@RequiredArgsConstructor
public class MainScreen extends SwingScreen {

    private static final String WINDOW_TITLE = "main";
    public static final int WIDTH = 256;
    public static final int HEIGHT = 240;

    private final Controller controller;

    public void init() {
        super.init(WINDOW_TITLE, WIDTH, HEIGHT);
        canvas.addKeyListener(controller);
    }

    public void refresh(MainScreenData data) {
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
