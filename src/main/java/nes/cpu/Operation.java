package nes.cpu;

import lombok.Getter;
import lombok.Value;

@Value
class Operation {
    private final Op op;
    @Getter
    private final AddressingMode addressingMode;
}
