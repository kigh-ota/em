package nes.ppu;

import common.BinaryUtil;
import common.ByteRegister;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class AddressRegister extends ByteRegister {
    private enum Latch {UPPER, LOWER} // TODO clear latch by reading StatusRegister
    private Latch next;

    final private PPU ppu;

    @Getter
    private int address;

    AddressRegister(PPU ppu) {
        super((byte)0);
        this.ppu = ppu;
        resetLatch();
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
            log.debug("addr={} (type={})", BinaryUtil.toHexString(address), MemoryMapper.getType(address));
            log.info(String.format("PPUaddr=%x", address));
        }
    }

    @Override
    public boolean increment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean decrement() {
        throw new UnsupportedOperationException();
    }

    void incrementAddress() {
        address += ppu.regPPUCTRL.addressIncrement();
    }

    void resetLatch() {
        next = Latch.UPPER;
    }
}
