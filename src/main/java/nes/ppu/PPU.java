package nes.ppu;

import common.ByteArrayMemory;
import common.ByteRegister;
import common.MemoryByte;
import lombok.Getter;

/**
 * http://hp.vector.co.jp/authors/VA042397/nes/ppu.html
 * ネームテーブルで設定された情報を使って、パターンテーブルからキャラクタを貼り付ける
 * 貼り付けたキャラクタに属性テーブルで指定された情報からパレットテーブルのパレットを使って色を加えて表示
 *
 * 画面構造
 * 256x240, 60fps
 *
 * パターンテーブル：キャラクタパターンを保存
 */
public class PPU {

    final MemoryMapper memoryMapper;

    final ByteArrayMemory characterRom;
    public final ByteArrayMemory nametables;
    public final ByteArrayMemory paletteRam;

    // https://wiki.nesdev.com/w/index.php/PPU_registers
    public final ControlRegister regPPUCTRL = new ControlRegister((byte)0);
    public final MemoryByte regPPUMASK = new MaskRegister((byte)0);
    public final MemoryByte regPPUSTATUS = new ByteRegister((byte)0);
    public final MemoryByte regOAMADDR = new ByteRegister((byte)0);
    public final MemoryByte regOAMDATA = new ByteRegister((byte)0);
    public final ScrollRegister regPPUSCROLL;
    public final AddressRegister regPPUADDR;
    public final MemoryByte regPPUDATA;

    @Getter
    private final Mirroring mirroring;

    public PPU(ByteArrayMemory characterRom, Mirroring mirroring) {
        memoryMapper = new MemoryMapper(this);
        this.characterRom = characterRom;
        nametables = new ByteArrayMemory(new byte[0x1000]);
        paletteRam = new ByteArrayMemory(new byte[0x20]);

        regPPUADDR = new AddressRegister(this);
        regPPUSCROLL = new ScrollRegister(this);
        regPPUDATA = new DataRegister(this);

        this.mirroring = mirroring;
    }

    public byte[] getCharacter(int table, int i) {
        int from = table * 0x1000 + i * 0x10;
        return this.characterRom.getRange(from, from + 16);
    }
}
