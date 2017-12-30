package common;

interface WritableByteRegister {

    default void set(byte value) {
        throw new UnsupportedOperationException();
    };

    default void setBit(boolean flag, int bit) {
        throw new UnsupportedOperationException();
    };

}
