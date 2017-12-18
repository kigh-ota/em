package nes.screen;

import java.awt.*;

import static com.google.common.base.Preconditions.checkArgument;

public class ScreenData {
    private Color[] values;
    private final int width;
    private final int height;

    public ScreenData(int width, int height) {
        this.width = width;
        this.height = height;
        values = new Color[width * height];
        clear();
    }

    public void clear() {
        for (int i = 0; i < width * height; i++) {
            values[i] = Color.BLACK;
        }
    }

    public Color get(int x, int y) {
        checkArgument(x >= 0 && x < width);
        checkArgument(y >= 0 && y < height);
        return values[y * width + x];
    }

    public void set(Color c, int x, int y) {
        checkArgument(x >= 0 && x < width);
        checkArgument(y >= 0 && y < height);
        values[y * width + x] = c;
    }
}
