package nes.ppu;

import com.google.common.collect.ImmutableList;
import java.awt.Color;
import java.util.List;

public class Palette {
    private static final List<Color> list;

    static {
        list = ImmutableList.<Color>builder().add(
                new Color(96, 96, 96),
                new Color(0, 32, 128),
                new Color(0, 0, 192),
                new Color(96, 64, 192),
                new Color(128, 0, 96),
                new Color(160, 0, 96),
                new Color(160, 32, 0),
                new Color(128, 64, 0),
                new Color(96, 64, 0),
                new Color(32, 64, 0),
                new Color(0, 96, 32),
                new Color(0, 128, 0),
                new Color(0, 64, 64),
                new Color(0, 0, 0),
                new Color(0, 0, 0),
                new Color(0, 0, 0),
                new Color(160, 160, 160),
                new Color(0, 96, 192),
                new Color(0, 64, 224),
                new Color(128, 0, 224),
                new Color(160, 0, 224),
                new Color(224, 0, 128),
                new Color(224, 0, 0),
                new Color(192, 96, 0),
                new Color(128, 96, 0),
                new Color(32, 128, 0),
                new Color(0, 128, 0),
                new Color(0, 160, 96),
                new Color(0, 128, 128),
                new Color(0, 0, 0),
                new Color(0, 0, 0),
                new Color(0, 0, 0),
                new Color(224, 224, 224),
                new Color(96, 160, 224),
                new Color(128, 128, 224),
                new Color(192, 96, 224),
                new Color(224, 0, 224),
                new Color(224, 96, 224),
                new Color(224, 128, 0),
                new Color(224, 160, 0),
                new Color(192, 192, 0),
                new Color(96, 192, 0),
                new Color(0, 224, 0),
                new Color(64, 224, 192),
                new Color(0, 224, 224),
                new Color(0, 0, 0),
                new Color(0, 0, 0),
                new Color(0, 0, 0),
                new Color(224, 224, 224),
                new Color(160, 192, 224),
                new Color(192, 160, 224),
                new Color(224, 160, 224),
                new Color(224, 128, 224),
                new Color(224, 160, 160),
                new Color(224, 192, 128),
                new Color(224, 224, 64),
                new Color(224, 224, 96),
                new Color(160, 224, 64),
                new Color(128, 224, 96),
                new Color(64, 224, 192),
                new Color(128, 192, 224),
                new Color(0, 0, 0),
                new Color(0, 0, 0),
                new Color(0, 0, 0)
                ).build();
    }

    static Color get(int i) {
        return list.get(i);
    }
}
