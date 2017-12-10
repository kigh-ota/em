package nes.ppu;

import common.BinaryUtil;
import common.ByteArrayMemory;

import static nes.ppu.PPU.OAM_SIZE;

public class ObjectAttributeMemory extends ByteArrayMemory {

    public ObjectAttributeMemory() {
        super(new byte[OAM_SIZE]);
    }

//    public Sprite getSprite(int n) {
//        checkArgument(n >= 0 && n < 64);
//        return new Sprite(getY(n), getTileIndex(n), getAttributes(n), getX(n));
//    }

    // TODO use the value of OAMADDR as offset

    public int getX(int n) {
        return Byte.toUnsignedInt(get(n * 4 + 3));
    }

    public int getY(int n) {
        return Byte.toUnsignedInt(get(n * 4)) + 1;
    }

    public int getTileIndex(int n) {
        return Byte.toUnsignedInt(get(n * 4 + 1));
    }

//    public Sprite.Attributes getAttributes(int n) {
//        byte value = get(n * 4 + 2);
//        return new Sprite.Attributes(
//                (value & 0b11) + 4,
//                !BinaryUtil.getBit(value, 5),
//                BinaryUtil.getBit(value, 6),
//                BinaryUtil.getBit(value, 7)
//        );
//    }

    public boolean isFlippedHorizontally(int n) {
        byte value = get(n * 4 + 2);
        return BinaryUtil.getBit(value, 6);
    }

    public boolean isFlippedVertically(int n) {
        byte value = get(n * 4 + 2);
        return BinaryUtil.getBit(value, 7);
    }

    /**
     *
     * @param n 0-63
     * @return 4-7
     */
    public int getPalette(int n) {
        byte value = get(n * 4 + 2);
        return (value & 0b11) + 4;
    }
}
