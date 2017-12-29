package nes.apu;

public class TriangleTimerHighRegister extends TimerHighRegister {

    private final TriangleChannel triangle;

    TriangleTimerHighRegister(TriangleChannel channel, APU apu) {
        super(channel, apu);
        this.triangle = channel;
    }

    @Override
    public void set(byte value) {
        super.set(value);
        triangle.getLinearCounter().setReloadFlag(true);
    }
}
