package nes.screen;

import java.awt.*;

import static com.google.common.base.Preconditions.checkArgument;
import static nes.screen.MainScreen.HEIGHT;
import static nes.screen.MainScreen.WIDTH;

public class MainScreenData {

    private Color[] values;

    public MainScreenData() {
        values = new Color[WIDTH * HEIGHT];
        clear();
    }

    public void clear() {
        for (int i = 0; i < WIDTH * HEIGHT; i++) {
            values[i] = Color.BLACK;
        }
    }

    public Color get(int x, int y) {
        checkArgument(x >= 0 && x < WIDTH);
        checkArgument(y >= 0 && y < HEIGHT);
        return values[y * WIDTH + x];
    }

    public void set(Color c, int x, int y) {
        checkArgument(x >= 0 && x < WIDTH);
        checkArgument(y >= 0 && y < HEIGHT);
        values[y * WIDTH + x] = c;
    }

}
