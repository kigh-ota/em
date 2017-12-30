package common;

public interface ReadableByteRegister {

    default byte get() {
        throw new UnsupportedOperationException();
    };

    default boolean getBit(int bit) {
        throw new UnsupportedOperationException();
    };

}
