package common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ByteArrayMemoryTest {
    ByteArrayMemory sut;
    private static final int SIZE = 8;

    @BeforeEach
    void setUp() {
        sut = new ByteArrayMemory(new byte[SIZE]);
    }

    @Test
    void testSetAndGet() {
        sut.set((byte)0xfe, 5);
        assertEquals((byte)0xfe, sut.get(5));
    }

    @Test
    void getRange() {
        sut.set((byte)0xfd, 5);
        sut.set((byte)0xfe, 6);
        sut.set((byte)0xff, 7);
        byte[] actual = sut.getRange(5, 8);
        assertEquals(3, actual.length);
        assertEquals((byte)0xfd, actual[0]);
        assertEquals((byte)0xfe, actual[1]);
        assertEquals((byte)0xff, actual[2]);
    }

    @Test
    void increment() {
        final int OFFSET = 1;
        sut.set((byte)0xfe, OFFSET);
        sut.increment(OFFSET);
        assertEquals((byte)0xff, sut.get(OFFSET));
        sut.increment(OFFSET);
        assertEquals((byte)0x00, sut.get(OFFSET));
    }

    @Test
    void decrement() {
        final int OFFSET = 1;
        sut.set((byte)0x01, OFFSET);
        sut.decrement(OFFSET);
        assertEquals((byte)0x00, sut.get(OFFSET));
        sut.decrement(OFFSET);
        assertEquals((byte)0xff, sut.get(OFFSET));
    }

}