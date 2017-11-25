package common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ByteRegisterTest {
    @Test
    void testIncrement() {
        ByteRegister sut = new ByteRegister((byte) 0b11111110);
        assertFalse(sut.increment());
        assertEquals((byte)0b11111111, sut.get());
        assertTrue(sut.increment());
        assertEquals(0, sut.get());
    }

    @Test
    void testDecrement() {
        ByteRegister sut = new ByteRegister((byte)1);
        assertFalse(sut.decrement());
        assertEquals((byte)0, sut.get());
        assertTrue(sut.decrement());
        assertEquals((byte)0b11111111, sut.get());
    }

    @Test
    void testAdd() {
        ByteRegister sut = new ByteRegister((byte)0b11110000);
        assertTrue(sut.add((byte)0b00010001));
        assertEquals((byte)0b00000001, sut.get());
    }

    @Test
    void testSubtract() {
        ByteRegister sut = new ByteRegister((byte)0b00000001);
        assertTrue(sut.subtract((byte)0b00000010));
        assertEquals((byte)0b11111111, sut.get());
    }
}