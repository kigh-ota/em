package common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BinaryUtilTest {
    @Test
    void toBinaryStringTest() {
        String actual = BinaryUtil.toBinaryString(3, 5);
        assertEquals("00011", actual);
    }
}