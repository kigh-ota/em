package nes.screen;

import java.awt.*;

import static com.google.common.base.Preconditions.checkArgument;
import static nes.screen.MainScreen.HEIGHT;
import static nes.screen.MainScreen.WIDTH;

public class MainScreenData {
    private Color[] value = new Color[WIDTH * HEIGHT];

    public Color get(int x, int y) {
        checkArgument(x >= 0 && x < WIDTH);
        checkArgument(y >= 0 && y < HEIGHT);
        return value[y * WIDTH + x];
    }

    public void set(Color c, int x, int y) {
        checkArgument(x >= 0 && x < WIDTH);
        checkArgument(y >= 0 && y < HEIGHT);
        value[y * WIDTH + x] = c;
    }

}
