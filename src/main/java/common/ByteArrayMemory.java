package common;

import org.eclipse.collections.api.tuple.Pair;

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

    // 計算後の値を返す
    public byte increment(int offset) {
        Pair<Byte, Boolean> pair = BinaryUtil.add(data[offset], (byte) 1);
        return pair.getOne();
    }

    // 計算後の値を返す
    public byte decrement(int offset) {
        Pair<Byte, Boolean> pair = BinaryUtil.subtract(data[offset], (byte) 1);
        data[offset]--;
        return data[offset];
    }
}
