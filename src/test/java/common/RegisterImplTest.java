package common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterImplTest {
    @Test
    void constructorTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RegisterImpl(0, 0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new RegisterImpl(0, 32);
        });
    }

    @Test
    void incrementTest() {
        RegisterImpl register = new RegisterImpl(14, 4);
        boolean carry = register.increment();
        assertFalse(carry);
        assertEquals(15, register.get());
        carry = register.increment();
        assertTrue(carry);
        assertEquals(0, register.get());
    }

    @Test
    void decrementTest() {
        RegisterImpl register = new RegisterImpl(0, 4);
        register.decrement();
        assertEquals(15, register.get());
    }

    @Test
    void addTest() {
        RegisterImpl register = new RegisterImpl(14, 4);
        boolean carry = register.add(2);
        assertTrue(carry);
        assertEquals(0, register.get());
    }

    @Test
    void maxValueTest() {
        RegisterImpl register = new RegisterImpl(0, 7);
        assertEquals(0b1111111, register.maxValue());
    }
}