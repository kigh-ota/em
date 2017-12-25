package common;

/**
 * バイト値を持つレジスタ
 */
public class ByteRegisterImpl implements ByteRegister {
    private byte value;

    public ByteRegisterImpl(byte value) {
        this.value = value;
    }

    @Override
    public void set(byte value) {
        this.value = value;
    }

    @Override
    public byte get() {
        return value;
    }

    @Override
    public boolean increment() {
        if (value == (byte)MAX_VALUE) {
            value = 0;
            return true;
        }
        value++;
        return false;
    }

    @Override
    public boolean decrement() {
        if (value == 0) {
            value = (byte)MAX_VALUE;
            return true;
        }
        value--;
        return false;
    }


    @Override
    public boolean add(byte addend) {
        int result = Byte.toUnsignedInt(value) + Byte.toUnsignedInt(addend);
        if (result > MAX_VALUE) {
            value = (byte)(result - (MAX_VALUE + 1));
            return true;
        }
        value = (byte)result;
        return false;
    }

    @Override
    public boolean subtract(byte subtrahend) {
        int result = Byte.toUnsignedInt(value) - Byte.toUnsignedInt(subtrahend);
        if (result < 0) {
            value = (byte)(result + (MAX_VALUE + 1));
            return true;
        }
        value = (byte)result;
        return false;
    }

    @Override
    public boolean getBit(int bit) {
        return BinaryUtil.getBit(value, bit);
    }

    @Override
    public void setBit(boolean flag, int bit) {
        value = BinaryUtil.setBit(flag, value, bit);
    }
}
