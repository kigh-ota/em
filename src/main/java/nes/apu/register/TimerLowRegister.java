package nes.apu.register;

import common.ByteRegister;
import nes.apu.channel.Channel;

public class TimerLowRegister implements ByteRegister {

    private final Channel pulse;

    public TimerLowRegister(Channel pulse) {
        this.pulse = pulse;
    }

    @Override
    public void set(byte value) {
        pulse.setTimerLow(Byte.toUnsignedInt(value));
    }
}
