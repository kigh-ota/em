package common;

import java.util.Arrays;

public class ByteArrayMemory implements MemoryByte {
    private byte[] data;
    private int offset;

    public ByteArrayMemory(byte[] data) {
        this.data = data;
        offset = 0;
    }

    public MemoryByte at(int offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public void set(byte value) {
        data[offset] = value;
    }

    @Override
    public byte get() {
        return data[offset];
    }

    public byte[] getRange(int from, int to) {
        return Arrays.copyOfRange(data, from, to);
    }

    @Override
    public boolean increment() {
        if (data[offset] == (byte)MAX_VALUE) {
            data[offset] = 0;
            return true;
        }
        data[offset]++;
        return false;
    }

    @Override
    public boolean decrement() {
        if (data[offset] == 0) {
            data[offset] = (byte)MAX_VALUE;
            return true;
        }
        data[offset]--;
        return false;
    }

    @Override
    public boolean add(byte addend) {
        int result = Byte.toUnsignedInt(data[offset]) + Byte.toUnsignedInt(addend);
        if (result > MAX_VALUE) {
            data[offset] = (byte)(result - (MAX_VALUE + 1));
            return true;
        }
        data[offset] = (byte)result;
        return false;
    }

    @Override
    public boolean subtract(byte subtrahend) {
        int result = Byte.toUnsignedInt(data[offset]) - Byte.toUnsignedInt(subtrahend);
        if (result < 0) {
            data[offset] = (byte)(result + (MAX_VALUE + 1));
            return true;
        }
        data[offset] = (byte)result;
        return false;
    }

    @Override
    public boolean getBit(int bit) {
        return BinaryUtil.getBit(data[offset], bit);
    }
}
