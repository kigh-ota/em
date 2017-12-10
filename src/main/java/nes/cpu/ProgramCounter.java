package nes.cpu;

import common.RegisterImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProgramCounter extends RegisterImpl {
    public ProgramCounter(int value, int width) {
        super(value, width);
    }

    @Override
    public void set(int value) {
        log.debug("jump to: {}", Integer.toHexString(value));
        super.set(value);
    }
}
