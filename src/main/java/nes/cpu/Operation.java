package nes.cpu;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.tuple.Tuples;

import java.util.Map;

import static nes.cpu.AddressingMode.*;
import static nes.cpu.Opcode.*;

@RequiredArgsConstructor
class Operation {
    @Getter
    private final Opcode opcode;
    @Getter
    private final AddressingMode addressingMode;

    private static Map<Pair<Opcode, AddressingMode>, Operation> instanceMap = Maps.mutable.empty();

    private static Operation getInstance(Opcode opcode, AddressingMode addressingMode) {
        Pair<Opcode, AddressingMode> key = Tuples.pair(opcode, addressingMode);
        Operation operation = instanceMap.get(key);
        if (operation == null) {
            operation = new Operation(opcode, addressingMode);
            instanceMap.put(key, operation);
        }
        return operation;
    }

    static Operation of(byte code) {
        switch (Byte.toUnsignedInt(code)) {
            // FIXME incomplete
            case 0xD0:
                return getInstance(BNE, RELATIVE);
            case 0x10:
                return getInstance(BPL, RELATIVE);

            case 0x4C:
                return getInstance(JMP, ABSOLUTE);
            case 0x6C:
                return getInstance(JMP, INDIRECT);

            case 0xE8:
                return getInstance(INX, IMPLICIT);
            case 0xC8:
                return getInstance(INY, IMPLICIT);
            case 0xCA:
                return getInstance(DEX, IMPLICIT);
            case 0x88:
                return getInstance(DEY, IMPLICIT);

            case 0x78:
                return getInstance(SEI, IMPLICIT);

            case 0xA9:
                return getInstance(LDA, IMMEDIATE);
            case 0xA5:
                return getInstance(LDA, ZERO_PAGE);
            case 0xB5:
                return getInstance(LDA, ZERO_PAGE_X);
            case 0xAD:
                return getInstance(LDA, ABSOLUTE);
            case 0xBD:
                return getInstance(LDA, ABSOLUTE_X);
            case 0xB9:
                return getInstance(LDA, ABSOLUTE_Y);
            case 0xA1:
                return getInstance(LDA, INDEXED_INDIRECT_X);
            case 0xB1:
                return getInstance(LDA, INDIRECT_INDEXED_Y);

            case 0xA2:
                return getInstance(LDX, IMMEDIATE);
            case 0xA6:
                return getInstance(LDX, ZERO_PAGE);
            case 0xB6:
                return getInstance(LDX, ZERO_PAGE_Y);
            case 0xAE:
                return getInstance(LDX, ABSOLUTE);
            case 0xBE:
                return getInstance(LDX, ABSOLUTE_Y);

            case 0xA0:
                return getInstance(LDY, IMMEDIATE);
            case 0xA4:
                return getInstance(LDY, ZERO_PAGE);
            case 0xB4:
                return getInstance(LDY, ZERO_PAGE_X);
            case 0xAC:
                return getInstance(LDY, ABSOLUTE);
            case 0xBC:
                return getInstance(LDY, ABSOLUTE_X);

            case 0x85:
                return getInstance(STA, ZERO_PAGE);
            case 0x95:
                return getInstance(STA, ZERO_PAGE_X);
            case 0x8D:
                return getInstance(STA, ABSOLUTE);
            case 0x9D:
                return getInstance(STA, ABSOLUTE_X);
            case 0x99:
                return getInstance(STA, ABSOLUTE_Y);
            case 0x81:
                return getInstance(STA, INDEXED_INDIRECT_X);
            case 0x91:
                return getInstance(STA, INDIRECT_INDEXED_Y);

            case 0x86:
                return getInstance(STX, ZERO_PAGE);
            case 0x96:
                return getInstance(STX, ZERO_PAGE_Y);
            case 0x8E:
                return getInstance(STX, ABSOLUTE);

            case 0x9A:
                return getInstance(TXS, IMPLICIT);

            case 0xD8:
                return getInstance(CLD, IMPLICIT);

            case 0x09:
                return getInstance(ORA, IMMEDIATE);
            case 0x05:
                return getInstance(ORA, ZERO_PAGE);
            case 0x15:
                return getInstance(ORA, ZERO_PAGE_X);
            case 0x0D:
                return getInstance(ORA, ABSOLUTE);
            case 0x1D:
                return getInstance(ORA, ABSOLUTE_X);
            case 0x19:
                return getInstance(ORA, ABSOLUTE_Y);
            case 0x01:
                return getInstance(ORA, INDEXED_INDIRECT_X);
            case 0x11:
                return getInstance(ORA, INDIRECT_INDEXED_Y);

            case 0x00:
                return getInstance(BRK, IMPLICIT);

            case 0x24:
                return getInstance(BIT, ZERO_PAGE);
            case 0x2C:
                return getInstance(BIT, ABSOLUTE);

            case 0xAA:
                return getInstance(TAX, ACCUMULATOR);

            case 0xE0:
                return getInstance(CPX, IMMEDIATE);
            case 0xE4:
                return getInstance(CPX, ZERO_PAGE);
            case 0xEC:
                return getInstance(CPX, ABSOLUTE);

            case 0xC9:
                return getInstance(CMP, IMMEDIATE);
            case 0xC5:
                return getInstance(CMP, ZERO_PAGE);
            case 0xD5:
                return getInstance(CMP, ZERO_PAGE_X);
            case 0xCD:
                return getInstance(CMP, ABSOLUTE);
            case 0xDD:
                return getInstance(CMP, ABSOLUTE_X);
            case 0xD9:
                return getInstance(CMP, ABSOLUTE_Y);
            case 0xC1:
                return getInstance(CMP, INDEXED_INDIRECT_X);
            case 0xD1:
                return getInstance(CMP, INDIRECT_INDEXED_Y);

            case 0x90:
                return getInstance(BCC, RELATIVE);

            case 0x48:
                return getInstance(PHA, IMPLICIT);

            case 0x68:
                return getInstance(PLA, IMPLICIT);

            case 0x20:
                return getInstance(JSR, ABSOLUTE);

            case 0x60:
                return getInstance(RTS, IMPLICIT);
        }
        return null;
    }
}
