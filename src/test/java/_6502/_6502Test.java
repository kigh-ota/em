package _6502;

import nes.cpu._6502;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class _6502Test {
    @Test
    void getAddressTest() {
        int address = _6502.getAddress((byte)0x01, (byte)0x20);
        assertEquals(0x2001, address);
    }
}