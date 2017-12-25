package common;

/**
 * バイト値を持つレジスタ
 */
public class ByteRegister {
    private byte value;

    static private final int MAX_VALUE = 0b11111111;

    public ByteRegister(byte value) {
        this.value = value;
    }

    public void set(byte value) {
        this.value = value;
    }

    public byte get() {
        return value;
    }

    public boolean increment() {
        if (value == (byte)MAX_VALUE) {
            value = 0;
            return true;
        }
        value++;
        return false;
    }

    public boolean decrement() {
        if (value == 0) {
            value = (byte)MAX_VALUE;
            return true;
        }
        value--;
        return false;
    }


    public boolean add(byte addend) {
        int result = Byte.toUnsignedInt(value) + Byte.toUnsignedInt(addend);
        value = (byte)result;
        return result > MAX_VALUE;
    }

    public boolean subtract(byte subtrahend) {
        int result = Byte.toUnsignedInt(value) - Byte.toUnsignedInt(subtrahend);
        value = (byte)result;
        return result < 0;
    }

    public boolean getBit(int bit) {
        return BinaryUtil.getBit(value, bit);
    }

    public void setBit(boolean flag, int bit) {
        value = BinaryUtil.setBit(flag, value, bit);
    }
}
