package common;

import java.util.Arrays;

public class ByteArrayMemory {
    private byte[] data;

    public ByteArrayMemory(byte[] data) {
        this.data = data;
    }

    public void set(byte value, int offset) {
        data[offset] = value;
    }

    public byte get(int offset) {
        return data[offset];
    }

    public byte[] getRange(int from, int to) {
        return Arrays.copyOfRange(data, from, to);
    }
}
