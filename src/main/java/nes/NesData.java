package nes;

import common.BinaryUtil;
import common.ByteArrayMemory;
import nes.ppu.Mirroring;

// https://wiki.nesdev.com/w/index.php/INES
public class NesData {
    // Header: 16 bytes
    // Trainer (if present)
    public ByteArrayMemory programRom;   // PRG ROM: 16384*x bytes
    public ByteArrayMemory characterRom; // CHR ROM (if present)
    // PlayChoice INST-ROM (if present)
    // PlayChoice PROM (if present)

    final Mirroring mirroring;

    public NesData(byte flag6) {
        mirroring = BinaryUtil.getBit(flag6, 0) ? Mirroring.VERTICAL : Mirroring.HORIZONTAL;
    }
}
