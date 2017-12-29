package nes.apu;

public class PulseHighRegister extends APURegister {

    private final Pulse pulse;

    PulseHighRegister(Pulse pulse, APU apu) {
        super(apu);
        this.pulse = pulse;
    }

    @Override
    public void set(byte value) {
        pulse.setTimerHigh(Byte.toUnsignedInt(value) & 0b00000111);
        pulse.resetSequencerPhase();
        pulse.getEnvelope().setStartFlag(true);
    }
}
