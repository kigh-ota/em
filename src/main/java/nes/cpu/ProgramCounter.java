package nes.cpu;

import common.BinaryUtil;
import common.IntegerRegister;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProgramCounter extends IntegerRegister {
    public ProgramCounter(int value, int width) {
        super(value, width);
    }

    @Override
    public void set(int value) {
        log.debug("jump to: {}", BinaryUtil.toHexString(value));
        super.set(value);
    }
}
