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

    @Getter
    private int tempAddress;

    AddressRegister(PPU ppu) {
        super((byte)0);
        this.ppu = ppu;
        tempAddress = 0;
        address = 0;
    }

    @Override
    public void set(byte value) {
        if (!ppu.addressLatch) {
            tempAddress = (tempAddress & 0x00ff) | Byte.toUnsignedInt(value) << 8;
        } else {
            tempAddress = (tempAddress & 0xff00) | Byte.toUnsignedInt(value);
            address = tempAddress;
            if (log.isDebugEnabled()) {
                log.debug("addr={} (type={})", BinaryUtil.toHexString(address), MemoryMapper.getType(address));
            }
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
