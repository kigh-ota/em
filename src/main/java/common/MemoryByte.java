package common;

public interface MemoryByte extends ReadableByte, WritableByte {
    static final int MAX_VALUE = 0b11111111;

    boolean increment();
    boolean decrement();
    boolean add(byte addend);
    boolean subtract(byte subtrahend);
    boolean getBit(int bit);
}
