package nes.apu.register;

import nes.apu.channel.PulseChannel;

public class PulseTimerHighRegister extends TimerHighRegister {

    private final PulseChannel pulse;

    public PulseTimerHighRegister(PulseChannel channel) {
        super(channel);
        pulse = channel;
    }

    @Override
    public void set(byte value) {
        super.set(value);

        pulse.resetSequencerPhase();
        pulse.getEnvelope().setStartFlag(true);
    }
}
