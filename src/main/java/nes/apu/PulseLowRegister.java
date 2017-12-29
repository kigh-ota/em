package nes.apu;

public class PulseLowRegister extends APURegister {

    private final PulseChannel pulse;

    PulseLowRegister(PulseChannel pulse, APU apu) {
        super(apu);
        this.pulse = pulse;
    }

    @Override
    public void set(byte value) {
        pulse.setTimerLow(Byte.toUnsignedInt(value));
    }
}
