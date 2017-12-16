package nes.cpu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class OpTest {
    @Nested
    class ExecuteTest {

        CPU cpu;

        @BeforeEach
        void setUp() {
            cpu = mock(CPU.class);
            doCallRealMethod().when(cpu).setZeroFlag(anyByte());
            doCallRealMethod().when(cpu).setNegativeFlag(anyByte());
        }

        // ADC

        @Test
        void testADC() {
            // 16 + 16 + 1 = 33
            when(cpu.getA()).thenReturn((byte)0x10);
            when(cpu.getCarryFlag()).thenReturn(true);

            Op.ADC.execute(null, (byte)0x10, cpu);

            expectA(0x21);
            expectZeroFlag(false);
            expectCarryFlag(false);
            expectNegativeFlag(false);
            expectOverflowFlag(false);
        }

        @Test
        void testADC_NegativeCarry() {
            // -1 + (-1) + 1 = -1
            when(cpu.getA()).thenReturn((byte)0b11111111);
            when(cpu.getCarryFlag()).thenReturn(true);

            Op.ADC.execute(null, (byte)0b11111111, cpu);

            expectA(0b11111111);
            expectZeroFlag(false);
            expectCarryFlag(true);
            expectNegativeFlag(true);
            expectOverflowFlag(false);
        }

        @Test
        void testADC_Overflow() {
            // 127 + 1 = 128 (-128)
            when(cpu.getA()).thenReturn((byte)0b01111111);
            when(cpu.getCarryFlag()).thenReturn(false);

            Op.ADC.execute(null, (byte)0b00000001, cpu);

            expectA(0b10000000);
            expectZeroFlag(false);
            expectCarryFlag(false);
            expectNegativeFlag(true);
            expectOverflowFlag(true);
        }

        // SBC

        @Test
        void testSBC_Carry() {
            // 16 - 1 - 1 = 14
            when(cpu.getA()).thenReturn((byte)0x10);
            when(cpu.getCarryFlag()).thenReturn(false);

            Op.SBC.execute(null, (byte)0x01, cpu);

            expectA(0x0e);
            expectZeroFlag(false);
            expectCarryFlag(true);
            expectNegativeFlag(false);
            expectOverflowFlag(false);
        }

        @Test
        void testSBC_Negative() {
            // 127 - 127 - 1 = -1
            when(cpu.getA()).thenReturn((byte)0b01111111); // +127
            when(cpu.getCarryFlag()).thenReturn(false); // --

            Op.SBC.execute(null, (byte)0b01111111, cpu); // +127

            expectA(0b11111111); // -1
            expectZeroFlag(false);
            expectCarryFlag(false);
            expectNegativeFlag(true);
            expectOverflowFlag(false);
        }

        @Test
        void testSBC_OverflowNegativeCarry() {
            // 127 - (-1) = 128 (-128)
            when(cpu.getA()).thenReturn((byte)0b01111111);
            when(cpu.getCarryFlag()).thenReturn(true);

            Op.SBC.execute(null, (byte)0b11111111, cpu);

            expectA(0b10000000);
            expectZeroFlag(false);
            expectCarryFlag(true);
            expectNegativeFlag(true);
            expectOverflowFlag(true);
        }

        private void expectA(int value) {
            byte b = (byte)value;
            verify(cpu).setA((byte)value);
        }

        private void expectZeroFlag(boolean value) {
            verify(cpu).setZeroFlag(value);
        }

        private void expectNegativeFlag(boolean value) {
            verify(cpu).setNegativeFlag(value);
        }

        private void expectOverflowFlag(boolean value) {
            verify(cpu).setOverflowFlag(value);
        }

        private void expectCarryFlag(boolean value) {
            verify(cpu).setCarryFlag(value);
        }
    }


}