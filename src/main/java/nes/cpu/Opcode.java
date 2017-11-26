package nes.cpu;

import lombok.RequiredArgsConstructor;

// @see http://obelisk.me.uk/6502/reference.html
// @see http://pgate1.at-ninja.jp/NES_on_FPGA/nes_cpu.htm
@RequiredArgsConstructor
enum Opcode {
    ADC(true), // Add with Carry
    SBC(true), // Subtract with Carry

    AND(true), // Logical AND
    ORA(true), // Logical Inclusive OR
    EOR(true), // Exclusive OR

    ASL(false), // Arithmetic Shift Left
    LSR(false), // Logical Shift Right
    ROL(false), // Rotate Left
    ROR(false), // Rotate Right

    BCC(false), // Branch if Carry Clear
    BCS(false), // Branch if Carry Set
    BEQ(false), // Branch if Equal
    BMI(false), // Branch if Minus
    BNE(false), // Branch if Not Equal
    BPL(false), // Branch if Positive
    BVC(false), // Branch if Overflow Clear
    BVS(false), // Branch if Overflow Set

    BIT(true), // Bit Test

    JMP(false), // Jump
    JSR(false), // Jump to Subroutine
    RTS(false), // Return from Subroutine

    BRK(false), // Force Interrupt
    RTI(false), // Return from Interrupt

    CMP(true), // Compare
    CPX(true), // Compare X Register
    CPY(true), // Compare Y Register

    INC(false), // Increment Memory
    INX(false), // Increment X Register
    INY(false), // Increment Y Register
    DEC(false), // Decrement Memory
    DEX(false), // Decrement X Register
    DEY(false), // Decrement Y Register

    SEC(false), // Set Carry Flag
    CLC(false), // Clear Carry Flag
    SED(false), // Set Decimal Flag
    CLD(false), // Clear Decimal Mode
    SEI(false), // Set Interrupt Disable
    CLI(false), // Clear Interrupt Disable
    CLV(false), // Clear Overflow Flag

    LDA(true), // Load Accumulator
    LDX(true), // Load X Register
    LDY(true), // Load Y Register
    STA(false), // Store Accumulator
    STX(false), // Store X Register
    STY(false), // Store Y Register

    TAX(false), // Transfer Accumulator to X
    TAY(false), // Transfer Accumulator to Y
    TSX(false), // Transfer Stack Pointer to X
    TXA(false), // Transfer X to Accumulator
    TYA(false), // Transfer Y to Accumulator
    TXS(false), // Transfer X to Stack Pointer

    PHA(false), // Push Accumulator
    PLA(false), // Pull Accumulator
    PHP(false), // Push Processor Status
    PLP(false), // Pull Processor Status

    NOP(false); // No Operation

//    abstract void execute(_6502 cpu);

    final boolean needsValue;
}
