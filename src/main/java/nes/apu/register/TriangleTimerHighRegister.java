package nes.apu.register;

import nes.apu.channel.TriangleChannel;

public class TriangleTimerHighRegister extends TimerHighRegister {

    private final TriangleChannel triangle;

    public TriangleTimerHighRegister(TriangleChannel channel) {
        super(channel);
        this.triangle = channel;
    }

    @Override
    public void set(byte value) {
        super.set(value);
        triangle.getLinearCounter().setReloadFlag(true);
    }
}
