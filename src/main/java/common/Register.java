package common;

public interface Register {
    int get();
    void set(int value);

    boolean increment();
    boolean decrement();
    boolean add(int addend);
    boolean subtract(byte subtrahend);
    boolean getBit(int bit);
    void setBit(boolean value, int bit);
}
