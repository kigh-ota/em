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

        // AND

        @Test
        void testAND() {
            when(cpu.getA()).thenReturn((byte)0b00001111);
            Op.AND.execute(null, (byte)0b11110000, cpu);
            expectA(0);
            expectZeroFlag(true);
            expectNegativeFlag(false);
        }

        // ORA

        @Test
        void testORA() {
            when(cpu.getA()).thenReturn((byte)0b00001111);
            Op.ORA.execute(null, (byte)0b11110000, cpu);
            expectA(0b11111111);
            expectZeroFlag(false);
            expectNegativeFlag(true);
        }

        // EOR

        @Test
        void testEOR() {
            when(cpu.getA()).thenReturn((byte)0b00110011);
            Op.EOR.execute(null, (byte)0b11110000, cpu);
            expectA(0b11000011);
            expectZeroFlag(false);
            expectNegativeFlag(true);
        }

        // ASL

        @Test
        void testASL() {
            Op.ASL.execute(null, (byte)0b11110000, cpu);
            expectA(0b11100000);
            expectZeroFlag(false);
            expectNegativeFlag(true);
            expectCarryFlag(true);
        }

        // LSR

        @Test
        void testLSR() {
            Op.LSR.execute(null, (byte)0b00001111, cpu);
            expectA(0b00000111);
            expectZeroFlag(false);
            expectNegativeFlag(false);
            expectCarryFlag(true);
        }

        // ROL

        @Test
        void testROL() {
            when(cpu.getCarryFlag()).thenReturn(true);
            Op.ROL.execute(null, (byte)0b01110000, cpu);
            expectA(0b11100001);
            expectZeroFlag(false);
            expectNegativeFlag(true);
            expectCarryFlag(false);
        }

        // ROR

        @Test
        void testROR() {
            when(cpu.getCarryFlag()).thenReturn(true);
            Op.ROR.execute(null, (byte)0b11110000, cpu);
            expectA(0b11111000);
            expectZeroFlag(false);
            expectNegativeFlag(true);
            expectCarryFlag(false);
        }

        // TODO branch instructions

        // BIT

        @Test
        void testBIT() {
            when(cpu.getA()).thenReturn((byte)0);
            Op.BIT.execute(null, (byte)0b11111111, cpu);
            expectOverflowFlag(true);
            expectNegativeFlag(true);
            expectZeroFlag(true);
        }

        // CMP

        @Test
        void testCMPPositive() {
            when(cpu.getA()).thenReturn((byte)0xfe);
            Op.CMP.execute(null, (byte)0xfd, cpu);
            expectCarryFlag(true);
            expectZeroFlag(false);
            expectNegativeFlag(false);
        }

        @Test
        void testCMPEqual() {
            when(cpu.getA()).thenReturn((byte)0xfe);
            Op.CMP.execute(null, (byte)0xfe, cpu);
            expectCarryFlag(true);
            expectZeroFlag(true);
            expectNegativeFlag(false);
        }

        @Test
        void testCMPNegative() {
            when(cpu.getA()).thenReturn((byte)0xfe);
            Op.CMP.execute(null, (byte)0xff, cpu);
            expectCarryFlag(false);
            expectZeroFlag(false);
            expectNegativeFlag(true);
        }

        // TODO CPX, CPY, increment, decrement, flags, load, store


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