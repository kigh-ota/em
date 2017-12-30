package nes.apu;

import nes.apu.channel.Channel;

public class TimerLowRegister extends APURegister {

    private final Channel pulse;

    TimerLowRegister(Channel pulse, APU apu) {
        super(apu);
        this.pulse = pulse;
    }

    @Override
    public void set(byte value) {
        pulse.setTimerLow(Byte.toUnsignedInt(value));
    }
}
