package nes.apu.register;

import common.ByteRegister;
import nes.apu.channel.ChannelWithLengthCounter;

public class TimerHighRegister implements ByteRegister {

    protected final ChannelWithLengthCounter channel;

    TimerHighRegister(ChannelWithLengthCounter channel) {
        this.channel = channel;
    }

    @Override
    public void set(byte value) {
        channel.getLengthCounter().setValue((Byte.toUnsignedInt(value) & 0b11111000) >> 3);
        channel.setTimerHigh(Byte.toUnsignedInt(value) & 0b00000111);
    }
}
