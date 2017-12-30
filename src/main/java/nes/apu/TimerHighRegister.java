package nes.apu;

public class TimerHighRegister extends APURegister {

    protected final ChannelWithLengthCounter channel;

    TimerHighRegister(ChannelWithLengthCounter channel, APU apu) {
        super(apu);
        this.channel = channel;
    }

    @Override
    public void set(byte value) {
        channel.getLengthCounter().setValue((Byte.toUnsignedInt(value) & 0b11111000) >> 3);
        channel.setTimerHigh(Byte.toUnsignedInt(value) & 0b00000111);
    }
}
