package nes.cpu;

import lombok.Data;

@Data
class FlagRegister {
    private boolean negative;
    private boolean overflow;
    // one
    private boolean breakCommand;
    private boolean decimal;
    private boolean interruptDisable;
    private boolean zero;
    private boolean carry;

    FlagRegister() {
        // https://wiki.nesdev.com/w/index.php/CPU_power_up_state
        negative = false;
        overflow = false;
        breakCommand = true;
        decimal = false;
        interruptDisable = true;
        zero = false;
        carry = false;
    }
}
