package nes.apu;

import common.BinaryUtil;
import nes.apu.channel.NoiseChannel;
import nes.apu.channel.PulseChannel;
import nes.apu.channel.TriangleChannel;

public class StatusRegister extends APURegister {

    PulseChannel pulse1;
    PulseChannel pulse2;
    TriangleChannel triangle;
    NoiseChannel noise;

    public StatusRegister(PulseChannel pulse1, PulseChannel pulse2, TriangleChannel triangle, NoiseChannel noise, APU apu) {
        super(apu);
        this.pulse1 = pulse1;
        this.pulse2 = pulse2;
        this.triangle = triangle;
        this.noise = noise;
    }

    @Override
    public void set(byte value) {
        super.set(value);
        pulse1.setEnabled(BinaryUtil.getBit(value, 0));
        pulse2.setEnabled(BinaryUtil.getBit(value, 1));
        triangle.setEnabled(BinaryUtil.getBit(value, 2));
        noise.setEnabled(BinaryUtil.getBit(value, 3));
    }

    // TODO implement get()
}
