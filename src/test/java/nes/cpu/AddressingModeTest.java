package nes.cpu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AddressingModeTest {

    CPU cpuMock;

    @BeforeEach
    void setUp() {
        cpuMock = mock(CPU.class);
    }

    @Nested
    class GetAddressTest {
        @Test
        void testImplicit() {
            Integer actual = AddressingMode.IMPLICIT.getAddress(null, null, cpuMock);
            assertNull(actual);
        }

        @Test
        void testAccumulator() {
            Integer actual = AddressingMode.ACCUMULATOR.getAddress(null, null, cpuMock);
            assertNull(actual);
        }

        @Test
        void testImmediate() {
            Integer actual = AddressingMode.IMMEDIATE.getAddress((byte)0, null, cpuMock);
            assertNull(actual);
        }

        @Test
        void testImmediateThrows() {
            assertThrows(IllegalArgumentException.class, () -> {
                AddressingMode.IMMEDIATE.getAddress(null, null, cpuMock);
            });
        }

        @Test
        void testZeroPage() {
            assertEquals(0x00ff, (int)AddressingMode.ZERO_PAGE.getAddress((byte)0xff, null, cpuMock));
        }

        @Test
        void testZeroPageThrows() {
            assertThrows(IllegalArgumentException.class, () -> {
                AddressingMode.ZERO_PAGE.getAddress((byte)0, (byte)0, cpuMock);
            });
        }

        @Test
        void testZeroPageX() {
            when(cpuMock.getX()).thenReturn((byte)0xff);
            assertEquals(0x007f, (int)AddressingMode.ZERO_PAGE_X.getAddress((byte)0x80, null, cpuMock));
        }

        @Test
        void testZeroPageY() {
            when(cpuMock.getY()).thenReturn((byte)0xff);
            assertEquals(0x007f, (int)AddressingMode.ZERO_PAGE_Y.getAddress((byte)0x80, null, cpuMock));
        }

        @Test
        void testRelative() {
            when(cpuMock.getPC()).thenReturn(0x1111);
            assertEquals(0x1091, (int)AddressingMode.RELATIVE.getAddress((byte)0x80 /* -128 */, null, cpuMock));
        }

        @Test
        void testAbsolute() {
            assertEquals(0xffdd, (int)AddressingMode.ABSOLUTE.getAddress((byte)0xdd, (byte)0xff, cpuMock));

        }

        @Test
        void testAbsoluteX() {
            when(cpuMock.getX()).thenReturn((byte)0x02);
            assertEquals(0x2101, (int)AddressingMode.ABSOLUTE_X.getAddress((byte)0xff, (byte)0x20, cpuMock));
        }

        @Test
        void testAbsoluteY() {
            when(cpuMock.getY()).thenReturn((byte)0x06);
            assertEquals(0x5205, (int)AddressingMode.ABSOLUTE_Y.getAddress((byte)0xff, (byte)0x51, cpuMock));
        }

        @Test
        void testIndirect() {
            MemoryMapper mockMemoryMapper = mock(MemoryMapper.class);
            when(mockMemoryMapper.get(0x10ff)).thenReturn((byte)0x10);
            when(mockMemoryMapper.get(0x1000)).thenReturn((byte)0x20);
            when(mockMemoryMapper.get(0x1100)).thenReturn((byte)0x30);
            try {
                Field f = CPU.class.getDeclaredField("memoryMapper");
                f.setAccessible(true);
                f.set(cpuMock, mockMemoryMapper);
            } catch (Exception e) {}
            assertEquals(0x2010, (int)AddressingMode.INDIRECT.getAddress((byte)0xff, (byte)0x10, cpuMock));
        }

        @Test
        void testIndirectIndexedY() {
            MemoryMapper mockMemoryMapper = mock(MemoryMapper.class);
            when(mockMemoryMapper.get(0x00ff)).thenReturn((byte)0xff);
            when(mockMemoryMapper.get(0x0000)).thenReturn((byte)0x10);
            when(mockMemoryMapper.get(0x0100)).thenReturn((byte)0x20);
            try {
                Field f = CPU.class.getDeclaredField("memoryMapper");
                f.setAccessible(true);
                f.set(cpuMock, mockMemoryMapper);
            } catch (Exception e) {}
            when(cpuMock.getY()).thenReturn((byte)0x02);
            assertEquals(0x1101, (int)AddressingMode.INDIRECT_INDEXED_Y.getAddress((byte)0xff, null, cpuMock));

        }
    }
}