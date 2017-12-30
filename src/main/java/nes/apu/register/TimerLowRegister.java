package nes.apu.register;

import common.ByteRegister;
import nes.apu.channel.Channel;

public class TimerLowRegister extends ByteRegister {

    private final Channel pulse;

    public TimerLowRegister(Channel pulse) {
        super((byte)0);
        this.pulse = pulse;
    }

    @Override
    public void set(byte value) {
        pulse.setTimerLow(Byte.toUnsignedInt(value));
    }
}
