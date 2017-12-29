package nes.apu;

// Sweep
public class PulseSweepRegister extends APURegister {

    private final Pulse pulse;

    PulseSweepRegister(Pulse pulse, APU apu) {
        super(apu);
        this.pulse = pulse;
    }

    @Override
    public void set(byte value) {
    }
}
