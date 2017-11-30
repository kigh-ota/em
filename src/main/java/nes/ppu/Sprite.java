package nes.ppu;

import lombok.Value;

@Value
public class Sprite {
    int y;
    int tileIndex;
    Attributes attributes;
    int x;

    @Value
    public static class Attributes {
        int palette;    // 4-7
        boolean isFront;
        boolean flipHorizontally;
        boolean flipVertically;
    }

}
