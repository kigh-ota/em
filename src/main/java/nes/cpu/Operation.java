package nes.cpu;

import lombok.Getter;
import lombok.Value;

@Value
class Operation {
    private final Instruction instruction;
    @Getter
    private final AddressingMode addressingMode;
    @Getter
    private final int cycles;
}
