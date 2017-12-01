package nes.cpu;

import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.factory.Maps;

import java.util.Map;

import static nes.cpu.AddressingMode.*;
import static nes.cpu.Op.*;
import static org.eclipse.collections.impl.tuple.Tuples.pair;

class OperationFactory {
    private final Map<Byte, Operation> instanceMap;

    OperationFactory() {
        instanceMap = Maps.mutable.empty();

        putInstance(0x69, pair(ADC, IMMEDIATE));
        putInstance(0x65, pair(ADC, ZERO_PAGE));
        putInstance(0x75, pair(ADC, ZERO_PAGE_X));
        putInstance(0x6D, pair(ADC, ABSOLUTE));
        putInstance(0x7D, pair(ADC, ABSOLUTE_X));
        putInstance(0x79, pair(ADC, ABSOLUTE_Y));
        putInstance(0x61, pair(ADC, INDEXED_INDIRECT_X));
        putInstance(0x71, pair(ADC, INDIRECT_INDEXED_Y));

        putInstance(0xE9, pair(SBC, IMMEDIATE));
        putInstance(0xE5, pair(SBC, ZERO_PAGE));
        putInstance(0xF5, pair(SBC, ZERO_PAGE_X));
        putInstance(0xED, pair(SBC, ABSOLUTE));
        putInstance(0xFD, pair(SBC, ABSOLUTE_X));
        putInstance(0xF9, pair(SBC, ABSOLUTE_Y));
        putInstance(0xE1, pair(SBC, INDEXED_INDIRECT_X));
        putInstance(0xF1, pair(SBC, INDIRECT_INDEXED_Y));

        putInstance(0x29, pair(AND, IMMEDIATE));
        putInstance(0x25, pair(AND, ZERO_PAGE));
        putInstance(0x35, pair(AND, ZERO_PAGE_X));
        putInstance(0x2D, pair(AND, ABSOLUTE));
        putInstance(0x3D, pair(AND, ABSOLUTE_X));
        putInstance(0x39, pair(AND, ABSOLUTE_Y));
        putInstance(0x21, pair(AND, INDEXED_INDIRECT_X));
        putInstance(0x31, pair(AND, INDIRECT_INDEXED_Y));

        putInstance(0x49, pair(EOR, IMMEDIATE));
        putInstance(0x45, pair(EOR, ZERO_PAGE));
        putInstance(0x55, pair(EOR, ZERO_PAGE_X));
        putInstance(0x4D, pair(EOR, ABSOLUTE));
        putInstance(0x5D, pair(EOR, ABSOLUTE_X));
        putInstance(0x59, pair(EOR, ABSOLUTE_Y));
        putInstance(0x41, pair(EOR, INDEXED_INDIRECT_X));
        putInstance(0x51, pair(EOR, INDIRECT_INDEXED_Y));

        putInstance(0x09, pair(ORA, IMMEDIATE));
        putInstance(0x05, pair(ORA, ZERO_PAGE));
        putInstance(0x15, pair(ORA, ZERO_PAGE_X));
        putInstance(0x0D, pair(ORA, ABSOLUTE));
        putInstance(0x1D, pair(ORA, ABSOLUTE_X));
        putInstance(0x19, pair(ORA, ABSOLUTE_Y));
        putInstance(0x01, pair(ORA, INDEXED_INDIRECT_X));
        putInstance(0x11, pair(ORA, INDIRECT_INDEXED_Y));

        putInstance(0x0A, pair(ASL, ACCUMULATOR));
        putInstance(0x06, pair(ASL, ZERO_PAGE));
        putInstance(0x16, pair(ASL, ZERO_PAGE_X));
        putInstance(0x0E, pair(ASL, ABSOLUTE));
        putInstance(0x1E, pair(ASL, ABSOLUTE_X));

        putInstance(0x4A, pair(LSR, ACCUMULATOR));
        putInstance(0x46, pair(LSR, ZERO_PAGE));
        putInstance(0x56, pair(LSR, ZERO_PAGE_X));
        putInstance(0x4E, pair(LSR, ABSOLUTE));
        putInstance(0x5E, pair(LSR, ABSOLUTE_X));

        putInstance(0x2A, pair(ROL, ACCUMULATOR));
        putInstance(0x26, pair(ROL, ZERO_PAGE));
        putInstance(0x36, pair(ROL, ZERO_PAGE_X));
        putInstance(0x2E, pair(ROL, ABSOLUTE));
        putInstance(0x3E, pair(ROL, ABSOLUTE_X));

        putInstance(0x6A, pair(ROR, ACCUMULATOR));
        putInstance(0x66, pair(ROR, ZERO_PAGE));
        putInstance(0x76, pair(ROR, ZERO_PAGE_X));
        putInstance(0x6E, pair(ROR, ABSOLUTE));
        putInstance(0x7E, pair(ROR, ABSOLUTE_X));

        putInstance(0x90, pair(BCC, RELATIVE));
        putInstance(0xB0, pair(BCS, RELATIVE));
        putInstance(0xF0, pair(BEQ, RELATIVE));
        putInstance(0xD0, pair(BNE, RELATIVE));
        putInstance(0x30, pair(BMI, RELATIVE));
        putInstance(0x10, pair(BPL, RELATIVE));
        putInstance(0x50, pair(BVC, RELATIVE));
        putInstance(0x70, pair(BVS, RELATIVE));

        putInstance(0x24, pair(BIT, ZERO_PAGE));
        putInstance(0x2C, pair(BIT, ABSOLUTE));

        putInstance(0x18, pair(CLC, IMPLICIT));
        putInstance(0x38, pair(SEC, IMPLICIT));
        putInstance(0xD8, pair(CLD, IMPLICIT));
        putInstance(0xF8, pair(SED, IMPLICIT));
        putInstance(0x58, pair(CLI, IMPLICIT));
        putInstance(0x78, pair(SEI, IMPLICIT));
        putInstance(0xB8, pair(CLV, IMPLICIT));

        putInstance(0x00, pair(BRK, IMPLICIT));

        putInstance(0xC9, pair(CMP, IMMEDIATE));
        putInstance(0xC5, pair(CMP, ZERO_PAGE));
        putInstance(0xD5, pair(CMP, ZERO_PAGE_X));
        putInstance(0xCD, pair(CMP, ABSOLUTE));
        putInstance(0xDD, pair(CMP, ABSOLUTE_X));
        putInstance(0xD9, pair(CMP, ABSOLUTE_Y));
        putInstance(0xC1, pair(CMP, INDEXED_INDIRECT_X));
        putInstance(0xD1, pair(CMP, INDIRECT_INDEXED_Y));

        putInstance(0xE0, pair(CPX, IMMEDIATE));
        putInstance(0xE4, pair(CPX, ZERO_PAGE));
        putInstance(0xEC, pair(CPX, ABSOLUTE));

        putInstance(0xC0, pair(CPY, IMMEDIATE));
        putInstance(0xC4, pair(CPY, ZERO_PAGE));
        putInstance(0xCC, pair(CPY, ABSOLUTE));

        putInstance(0xC6, pair(DEC, ZERO_PAGE));
        putInstance(0xD6, pair(DEC, ZERO_PAGE_X));
        putInstance(0xCE, pair(DEC, ABSOLUTE));
        putInstance(0xDE, pair(DEC, ABSOLUTE_X));

        putInstance(0xE6, pair(INC, ZERO_PAGE));
        putInstance(0xF6, pair(INC, ZERO_PAGE_X));
        putInstance(0xEE, pair(INC, ABSOLUTE));
        putInstance(0xFE, pair(INC, ABSOLUTE_X));

        putInstance(0xCA, pair(DEX, IMPLICIT));
        putInstance(0x88, pair(DEY, IMPLICIT));
        putInstance(0xE8, pair(INX, IMPLICIT));
        putInstance(0xC8, pair(INY, IMPLICIT));

        putInstance(0x4C, pair(JMP, ABSOLUTE));
        putInstance(0x6C, pair(JMP, INDIRECT));

        putInstance(0x20, pair(JSR, ABSOLUTE));
        putInstance(0x60, pair(RTS, IMPLICIT));

        putInstance(0xA9, pair(LDA, IMMEDIATE));
        putInstance(0xA5, pair(LDA, ZERO_PAGE));
        putInstance(0xB5, pair(LDA, ZERO_PAGE_X));
        putInstance(0xAD, pair(LDA, ABSOLUTE));
        putInstance(0xBD, pair(LDA, ABSOLUTE_X));
        putInstance(0xB9, pair(LDA, ABSOLUTE_Y));
        putInstance(0xA1, pair(LDA, INDEXED_INDIRECT_X));
        putInstance(0xB1, pair(LDA, INDIRECT_INDEXED_Y));

        putInstance(0x85, pair(STA, ZERO_PAGE));
        putInstance(0x95, pair(STA, ZERO_PAGE_X));
        putInstance(0x8D, pair(STA, ABSOLUTE));
        putInstance(0x9D, pair(STA, ABSOLUTE_X));
        putInstance(0x99, pair(STA, ABSOLUTE_Y));
        putInstance(0x81, pair(STA, INDEXED_INDIRECT_X));
        putInstance(0x91, pair(STA, INDIRECT_INDEXED_Y));

        putInstance(0xA2, pair(LDX, IMMEDIATE));
        putInstance(0xA6, pair(LDX, ZERO_PAGE));
        putInstance(0xB6, pair(LDX, ZERO_PAGE_Y));
        putInstance(0xAE, pair(LDX, ABSOLUTE));
        putInstance(0xBE, pair(LDX, ABSOLUTE_Y));

        putInstance(0x86, pair(STX, ZERO_PAGE));
        putInstance(0x96, pair(STX, ZERO_PAGE_Y));
        putInstance(0x8E, pair(STX, ABSOLUTE));

        putInstance(0xA0, pair(LDY, IMMEDIATE));
        putInstance(0xA4, pair(LDY, ZERO_PAGE));
        putInstance(0xB4, pair(LDY, ZERO_PAGE_X));
        putInstance(0xAC, pair(LDY, ABSOLUTE));
        putInstance(0xBC, pair(LDY, ABSOLUTE_X));

        putInstance(0x84, pair(STY, ZERO_PAGE));
        putInstance(0x94, pair(STY, ZERO_PAGE_X));
        putInstance(0x8C, pair(STY, ABSOLUTE));

        putInstance(0xEA, pair(NOP, IMPLICIT));

        putInstance(0x48, pair(PHA, IMPLICIT));
        putInstance(0x68, pair(PLA, IMPLICIT));
        putInstance(0x08, pair(PHP, IMPLICIT));
        putInstance(0x28, pair(PLP, IMPLICIT));

        putInstance(0x40, pair(RTI, IMPLICIT));

        putInstance(0xAA, pair(TAX, IMPLICIT));
        putInstance(0x8A, pair(TXA, IMPLICIT));
        putInstance(0xA8, pair(TAY, IMPLICIT));
        putInstance(0x98, pair(TYA, IMPLICIT));
        putInstance(0xBA, pair(TSX, IMPLICIT));
        putInstance(0x9A, pair(TXS, IMPLICIT));
    }

    private void putInstance(int opcodeInt, Pair<Op, AddressingMode> value) {
        byte opcode = (byte)opcodeInt;
        if (instanceMap.containsKey(opcode)) {
            throw new IllegalArgumentException();
        }
        Op newOp = value.getOne();
        AddressingMode newAddressingMode = value.getTwo();
        Operation newOperation = new Operation(newOp, newAddressingMode);
        boolean dup = instanceMap.values().stream().anyMatch(operation -> {
            return operation.getOp() == newOp && operation.getAddressingMode() == newAddressingMode;
        });
        if (dup) {
            throw new IllegalArgumentException();
        }
        instanceMap.put(opcode, newOperation);
    }

    Operation get(byte opcode) {
        return instanceMap.get(opcode);
    }
}
