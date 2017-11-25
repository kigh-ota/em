package nes.ppu;

import common.ByteRegister;
import lombok.Getter;

import static nes.ppu.AddressRegister.Latch.LOWER;
import static nes.ppu.AddressRegister.Latch.UPPER;

public class AddressRegister extends ByteRegister {
    enum Latch {UPPER, LOWER} // TODO clear latch by reading StatusRegister

    final private PPU ppu;

    private Latch next;

    @Getter
    private int address;

    public AddressRegister(PPU ppu) {
        super((byte)0);
        this.ppu = ppu;
        next = UPPER;
        address = 0;
    }

    @Override
    public void set(byte value) {
        if (next == UPPER) {
            address = Byte.toUnsignedInt(value) << 8;
            next = LOWER;
        } else {
            address += Byte.toUnsignedInt(value);
            next = UPPER;
        }
    }

    public void incrementAddress() {
        address += ppu.regPPUCTRL.addressIncrement();
    }
}
