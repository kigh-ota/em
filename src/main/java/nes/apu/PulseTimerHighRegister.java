package nes.apu;

import nes.apu.channel.PulseChannel;

public class PulseTimerHighRegister extends TimerHighRegister {

    private final PulseChannel pulse;

    PulseTimerHighRegister(PulseChannel channel, APU apu) {
        super(channel, apu);
        pulse = channel;
    }

    @Override
    public void set(byte value) {
        super.set(value);

        pulse.resetSequencerPhase();
        pulse.getEnvelope().setStartFlag(true);
    }
}
