package nes.ppu;

import common.BinaryUtil;
import common.ByteArrayMemory;

import static com.google.common.base.Preconditions.checkArgument;
import static nes.ppu.PPU.OAM_SIZE;

public class ObjectAttributeMemory extends ByteArrayMemory {

    public ObjectAttributeMemory() {
        super(new byte[OAM_SIZE]);
    }

    public Sprite getSprite(int n) {
        checkArgument(n >= 0 && n < 64);
        return new Sprite(getY(n), getTileIndex(n), getAttributes(n), getX(n));
    }

    // TODO use the value of OAMADDR as offset

    private int getX(int n) {
        return Byte.toUnsignedInt(get(n * 4 + 3));
    }

    private int getY(int n) {
        return Byte.toUnsignedInt(get(n * 4)) + 1;
    }

    private int getTileIndex(int n) {
        return Byte.toUnsignedInt(get(n * 4 + 1));
    }

    private Sprite.Attributes getAttributes(int n) {
        byte value = get(n * 4 + 2);
        return new Sprite.Attributes(
                (value & 0b11) + 4,
                !BinaryUtil.getBit(value, 5),
                BinaryUtil.getBit(value, 6),
                BinaryUtil.getBit(value, 7)
        );
    }
}
