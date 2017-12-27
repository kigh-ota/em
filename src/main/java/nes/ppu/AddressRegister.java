package nes.ppu;

import common.BinaryUtil;
import common.ByteRegister;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class AddressRegister extends ByteRegister {

    final private PPU ppu;

    @Getter
    private int address;

    AddressRegister(PPU ppu) {
        super((byte)0);
        this.ppu = ppu;
        address = 0;
    }

    @Override
    public void set(byte value) {
        if (!ppu.addressLatch) {
            address = Byte.toUnsignedInt(value) << 8;
        } else {
            address += Byte.toUnsignedInt(value);
            log.debug("addr={} (type={})", BinaryUtil.toHexString(address), MemoryMapper.getType(address));
            log.info(String.format("PPUaddr=%x", address));
        }
        ppu.addressLatch = !ppu.addressLatch;
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
}
