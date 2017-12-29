package nes.apu;

public class PulseLowRegister extends APURegister {

    private final Pulse pulse;

    PulseLowRegister(Pulse pulse, APU apu) {
        super(apu);
        this.pulse = pulse;
    }

    @Override
    public void set(byte value) {
        pulse.setTimerLow(Byte.toUnsignedInt(value));
    }
}
