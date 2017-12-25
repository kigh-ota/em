package common;

public interface ByteRegister {
    static final int MAX_VALUE = 0b11111111;

    byte get();
    void set(byte value);

    boolean increment();
    boolean decrement();
    boolean add(byte addend);
    boolean subtract(byte subtrahend);
    boolean getBit(int bit);
    void setBit(boolean value, int bit);
}
