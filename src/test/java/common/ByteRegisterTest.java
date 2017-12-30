package common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ByteRegisterImplImplTest {
    @Test
    void testSet() {
        ByteRegisterImpl sut = new ByteRegisterImpl((byte)0);
        sut.set((byte)0xff);
        assertEquals((byte)0xff, sut.get());
    }

    @Test
    void testIncrement() {
        ByteRegisterImpl sut = new ByteRegisterImpl((byte) 0b11111110);
        assertFalse(sut.increment());
        assertEquals((byte)0b11111111, sut.get());
        assertTrue(sut.increment());
        assertEquals(0, sut.get());
    }

    @Test
    void testDecrement() {
        ByteRegisterImpl sut = new ByteRegisterImpl((byte)1);
        assertFalse(sut.decrement());
        assertEquals((byte)0, sut.get());
        assertTrue(sut.decrement());
        assertEquals((byte)0b11111111, sut.get());
    }

    @Test
    void testAddWithCarry() {
        ByteRegisterImpl sut = new ByteRegisterImpl((byte)0b11110000);
        assertTrue(sut.add((byte)0b00010001));
        assertEquals((byte)0b00000001, sut.get());
    }

    @Test
    void testAddWithoutCarry() {
        ByteRegisterImpl sut = new ByteRegisterImpl((byte)0b11110000);
        assertFalse(sut.add((byte)0b00001111));
        assertEquals((byte)0b11111111, sut.get());
    }

    @Test
    void testSubtractWithCarry() {
        ByteRegisterImpl sut = new ByteRegisterImpl((byte)0b00000001);
        assertTrue(sut.subtract((byte)0b00000010));
        assertEquals((byte)0b11111111, sut.get());
    }

    @Test
    void testSubtractWithoutCarry() {
        ByteRegisterImpl sut = new ByteRegisterImpl((byte)0b00000001);
        assertFalse(sut.subtract((byte)0b00000001));
        assertEquals((byte)0b00000000, sut.get());
    }

    @Test
    void setBitAndGetBitTest() {
        ByteRegisterImpl sut = new ByteRegisterImpl((byte)0b00000001);
        assertTrue(sut.getBit(0));
        assertFalse(sut.getBit(1));
        assertFalse(sut.getBit(2));
        assertFalse(sut.getBit(3));
        assertFalse(sut.getBit(4));
        assertFalse(sut.getBit(5));
        assertFalse(sut.getBit(6));
        assertFalse(sut.getBit(7));

        sut.setBit(true, 7);
        assertTrue(sut.getBit(0));
        assertFalse(sut.getBit(1));
        assertFalse(sut.getBit(2));
        assertFalse(sut.getBit(3));
        assertFalse(sut.getBit(4));
        assertFalse(sut.getBit(5));
        assertFalse(sut.getBit(6));
        assertTrue(sut.getBit(7));

        sut.setBit(false, 0);
        assertFalse(sut.getBit(0));
        assertFalse(sut.getBit(1));
        assertFalse(sut.getBit(2));
        assertFalse(sut.getBit(3));
        assertFalse(sut.getBit(4));
        assertFalse(sut.getBit(5));
        assertFalse(sut.getBit(6));
        assertTrue(sut.getBit(7));

        sut.setBit(false, 1);
        assertFalse(sut.getBit(0));
        assertFalse(sut.getBit(1));
        assertFalse(sut.getBit(2));
        assertFalse(sut.getBit(3));
        assertFalse(sut.getBit(4));
        assertFalse(sut.getBit(5));
        assertFalse(sut.getBit(6));
        assertTrue(sut.getBit(7));

    }

}