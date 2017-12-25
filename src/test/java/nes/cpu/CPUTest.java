package nes.cpu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CPUTest {
    @Test
    void getAddressTest() {
        int address = CPU.getAddress((byte)0x01, (byte)0x20);
        assertEquals(0x2001, address);
    }
}