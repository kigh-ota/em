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
    final ByteArrayMemory nametables;
    final ByteArrayMemory paletteRam;

    // https://wiki.nesdev.com/w/index.php/PPU_registers
    public final ControlRegister regPPUCTRL = new ControlRegister((byte)0);
    public final MemoryByte regPPUMASK = new MaskRegister((byte)0);
    public final MemoryByte regPPUSTATUS = new ByteRegister((byte)0b10000000 /* FIXME */);
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
        nametables = new ByteArrayMemory(new byte[0x0800]);
        paletteRam = new ByteArrayMemory(new byte[0x20]);

        regPPUADDR = new AddressRegister(this);
        regPPUSCROLL = new ScrollRegister(this);
        regPPUDATA = new DataRegister(this);

        this.mirroring = mirroring;
    }

    public byte[] getCharacterPattern(int table, int i) {
        int from = table * 0x1000 + i * 0x10;
        return this.characterRom.getRange(from, from + 16);
    }

    private static final int ATTRIBUTE_TABLE_OFFSET = 0x3C0;

    /**
     * @param nameTable 0-3
     * @param cell 0-959
     * @return 0-3
     */
    public int getPalette(int nameTable, int cell) {
        int base = nameTable * 0x400;
        int cellRow = cell / 32; // 0-29
        int cellCol = cell % 32; // 0-31
        int attributeRow = cellRow / 4; // 0-7
        int attributeCol = cellCol / 4; // 0-7
        boolean isUpper = cellRow % 2 == 0;
        boolean isLeft = cellCol % 2 == 0;

        byte attribute = nametables.get(base + attributeRow * 8 + attributeCol);
        int shift = (isUpper ? 0 : 4) + (isLeft ? 0 : 2);
        return (Byte.toUnsignedInt(attribute) >> shift) & 4;
    }

    public int getBackgroundPatternTable() {
        return regPPUCTRL.getBackgroundPatternTable();
    }

    /**
     *
     * @param nameTable 0-1
     * @param cell 0-959
     * @return 0-255
     */
    public int getCharacter(int nameTable, int cell) {
        return Byte.toUnsignedInt(nametables.get(nameTable * 0x400 + cell));
    }

    /**
     * @param palette 0-3
     * @param i 0-3
     * @return 0-63
     */
    public int getBackgroundColor(int palette, int i) {
        int offset = (i != 0) ? palette * 4 + i : 0;
        return paletteRam.get(offset);
    }
}
