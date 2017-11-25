package nes.cpu;

// @see http://obelisk.me.uk/6502/reference.html
// @see http://pgate1.at-ninja.jp/NES_on_FPGA/nes_cpu.htm
enum Opcode {
    ADC, // Add with Carry
    SBC, // Subtract with Carry

    AND, // Logical AND
    ORA, // Logical Inclusive OR
    EOR, // Exclusive OR

    ASL, // Arithmetic Shift Left
    LSR, // Logical Shift Right
    ROL, // Rotate Left
    ROR, // Rotate Right

    BCC, // Branch if Carry Clear
    BCS, // Branch if Carry Set
    BEQ, // Branch if Equal
    BMI, // Branch if Minus
    BNE, // Branch if Not Equal
    BPL, // Branch if Positive
    BVC, // Branch if Overflow Clear
    BVS, // Branch if Overflow Set

    BIT, // Bit Test

    JMP, // Jump
    JSR, // Jump to Subroutine
    RTS, // Return from Subroutine

    BRK, // Force Interrupt
    RTI, // Return from Interrupt

    CMP, // Compare
    CPX, // Compare X RegisterImpl
    CPY, // Compare Y RegisterImpl

    INC, // Increment Memory
    INX, // Increment X RegisterImpl
    INY, // Increment Y RegisterImpl
    DEC, // Decrement Memory
    DEX, // Decrement X RegisterImpl
    DEY, // Decrement Y RegisterImpl

    SEC, // Set Carry Flag
    CLC, // Clear Carry Flag
    SED, // Set Decimal Flag
    CLD, // Clear Decimal Mode
    SEI { // Set Interrupt Disable
//        @Override
//        void execute(_6502 cpu) {
//            cpu.regP.setInterruptDisable(true);
//        }
    },
    CLI, // Clear Interrupt Disable
    CLV, // Clear Overflow Flag

    LDA, // Load Accumulator
    LDX, // Load X RegisterImpl
    LDY, // Load Y RegisterImpl
    STA, // Store Accumulator
    STX, // Store X RegisterImpl
    STY, // Store Y RegisterImpl

    TAX, // Transfer Accumulator to X
    TAY, // Transfer Accumulator to Y
    TSX, // Transfer Stack Pointer to X
    TXA, // Transfer X to Accumulator
    TXS, // Transfer X to Stack Pointer
    TYA, // Transfer Y to Accumulator

    PHA, // Push Accumulator
    PLA, // Pull Accumulator
    PHP, // Push Processor Status
    PLP, // Pull Processor Status

    NOP, // No Operation

//    abstract void execute(_6502 cpu);
}
