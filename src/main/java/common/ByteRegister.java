package common;

/**
 * バイト値を持つレジスタ
 */
public interface ByteRegister extends ReadableByteRegister, WritableByteRegister {

    int MAX_VALUE = 0b11111111;

    default boolean increment() {
        throw new UnsupportedOperationException();
    };

    default boolean decrement() {
        throw new UnsupportedOperationException();
    }

    default boolean add(byte addend) {
        throw new UnsupportedOperationException();
    }

    default boolean subtract(byte subtrahend) {
        throw new UnsupportedOperationException();
    }

}
