package nes.ppu;

import common.ByteArrayMemory;
import common.ByteRegister;
import common.MemoryByte;
import lombok.Getter;
import lombok.Setter;
import nes.cpu.CPU;
import nes.screen.MainScreen;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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
public class PPU implements Runnable {

    private long cycles;

    @Setter
    private CPU cpu;

    private MainScreen mainScreen;

    static final int PALETTE_RAM_SIZE = 0x20;
    public static final int OAM_SIZE = 0x100;
    static final int NAMETABLE_MEMORY_SIZE = 0x800;

    final MemoryMapper memoryMapper;

    final ByteArrayMemory characterRom;
    final ByteArrayMemory nametables;
    final ByteArrayMemory paletteRam;
    public final ObjectAttributeMemory oam = new ObjectAttributeMemory();

    // https://wiki.nesdev.com/w/index.php/PPU_registers
    public final ControlRegister regPPUCTRL; // $2000
    public final MemoryByte regPPUMASK = new MaskRegister((byte)0);
    public final StatusRegister regPPUSTATUS; // $2002
    public final MemoryByte regOAMADDR = new ByteRegister((byte)0);
    public final MemoryByte regOAMDATA = new ByteRegister((byte)0); // TODO implement
    public final ScrollRegister regPPUSCROLL; // $2005
    public final AddressRegister regPPUADDR; // $2006
    public final MemoryByte regPPUDATA; // $2007

    @Getter
    private final Mirroring mirroring;

    public PPU(ByteArrayMemory characterRom, Mirroring mirroring, MainScreen mainScreen) {
        memoryMapper = new MemoryMapper(this);
        this.characterRom = characterRom;
        nametables = new ByteArrayMemory(new byte[NAMETABLE_MEMORY_SIZE]);
        paletteRam = new ByteArrayMemory(new byte[PALETTE_RAM_SIZE]);

        regPPUCTRL = new ControlRegister();
        regPPUSTATUS = new StatusRegister(this);
        regPPUSCROLL = new ScrollRegister(this);
        regPPUADDR = new AddressRegister(this);
        regPPUDATA = new DataRegister(this);

        this.mirroring = mirroring;

        this.mainScreen = mainScreen;
    }

    @Override
    public void run() {
        checkNotNull(mainScreen);
        mainScreen.init();

        checkNotNull(cpu);

        cycles = 0L;

        while (true) {
            if (shouldWaitCpu()) {
                continue;
            }

            // TODO draw single dot

            cycles++;
        }
    }

    private boolean shouldWaitCpu() {
        long cpuCycles = cpu.getCyclesSynchronized();
        return this.cycles >= cpuCycles * 3;
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

    public int getSpritePatternTable() {
        return regPPUCTRL.getSpritePatternTable();
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
     * @param palette 0-7
     * @param i 0-3
     * @return 0-63
     */
    public int getColor(int palette, int i) {
        checkArgument(palette >= 0 && palette < 8);
        checkArgument(i >= 0 && i < 4);
        int offset = (i != 0) ? palette * 4 + i : 0;
        return paletteRam.get(offset);
    }

    public boolean isCharacterRomAvailable() {
        return characterRom != null;
    }

}
