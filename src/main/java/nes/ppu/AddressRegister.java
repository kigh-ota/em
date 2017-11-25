package nes.ppu;

import common.ByteRegister;
import lombok.Getter;

class AddressRegister extends ByteRegister {
    private enum Latch {UPPER, LOWER} // TODO clear latch by reading StatusRegister
    private Latch next;

    final private PPU ppu;

    @Getter
    private int address;

    AddressRegister(PPU ppu) {
        super((byte)0);
        this.ppu = ppu;
        next = Latch.UPPER;
        address = 0;
    }

    @Override
    public void set(byte value) {
        if (next == Latch.UPPER) {
            address = Byte.toUnsignedInt(value) << 8;
            next = Latch.LOWER;
        } else {
            address += Byte.toUnsignedInt(value);
            next = Latch.UPPER;
        }
    }

    void incrementAddress() {
        address += ppu.regPPUCTRL.addressIncrement();
    }
}
