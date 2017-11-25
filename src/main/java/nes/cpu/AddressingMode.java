package nes.cpu;

// 1 page = 256 byte

enum AddressingMode {
    IMPLICIT(0),
    ACCUMULATOR(0),
    IMMEDIATE(1),   // 8 bit constant
    ZERO_PAGE(1),   // 8 bit address ($0000-$00FF) (LOWER) => $00:LOWER
    ZERO_PAGE_X(1), // (LOWER) => $00:LOWER+X
    ZERO_PAGE_Y(1),
    RELATIVE(1),    // 8 bit offset (=> -126 to +129)
    ABSOLUTE(2),    // 16 bit address ($0000-$FFFF) (LOWER,UPPER)
    ABSOLUTE_X(2),  // (LOWER,UPPER) => UPPER:LOWER+X
    ABSOLUTE_Y(2),
    INDIRECT(2), // (LOWER,UPPER) => value at UPPER:LOWER
    INDEXED_INDIRECT_X(1), // (LOWER) => value at $00:LOWER+X
    INDIRECT_INDEXED_Y(1); // (LOWER) => (value at $00:LOWER)+Y

    final int addressBytes;

    private AddressingMode(int addressBytes) {
        this.addressBytes = addressBytes;
    }
}
