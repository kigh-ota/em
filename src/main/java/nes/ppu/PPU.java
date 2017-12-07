package nes.ppu;

import common.BinaryUtil;
import common.ByteArrayMemory;
import common.ByteRegister;
import common.MemoryByte;
import lombok.Getter;
import lombok.Setter;
import nes.cpu.CPU;
import nes.screen.MainScreen;
import nes.screen.MainScreenData;

import java.awt.*;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static nes.screen.MainScreen.HEIGHT;
import static nes.screen.MainScreen.WIDTH;

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
    private long frames;

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
        frames = 0L;

        while (true) {
            if (shouldWaitCpu()) {
                continue;
            }

            // TODO NEXT draw dot by dot
//            cycles++;

            MainScreenData data = new MainScreenData();
            if (isCharacterRomAvailable()) {
                final int scrollX = regPPUSCROLL.getX();
                final int scrollY = regPPUSCROLL.getY();
                IntStream.range(0, HEIGHT).forEach(y -> {
                    IntStream.range(0, WIDTH).forEach(x -> {

                        Color c = findTopSprite(x, y).map(sprite -> {
                            int spritePatternTable = getSpritePatternTable();
                            byte[] pattern = getCharacterPattern(spritePatternTable, sprite.getTileIndex());
                            int color = getColorInPattern(x - sprite.getX(), y - sprite.getY(), pattern);
                            int colorIndex = getColorIndex(sprite.getAttributes().getPalette(), color);
                            return Palette.get(colorIndex);
                        }).orElseGet(() -> getBackgroundColor(x + scrollX, y + scrollY));

                        data.set(c, x, y);
                    });
                });
            }

            mainScreen.refresh(data);
            cycles += (frames % 2 == 0) ? 262 * 341 : 262 * 341 - 1;
            frames++;
        }
    }

    private Optional<Sprite> findTopSprite(int x, int y) {
        checkArgument(x >= 0 && x < WIDTH);
        checkArgument(y >= 0 && y < HEIGHT);
        checkArgument(regPPUCTRL.getSpriteSize() == ControlRegister.SpriteSize.EIGHT_BY_EIGHT); // TODO 8x16 sprite
        for (int i = 0; i < 64; i++) {
            Sprite sprite = oam.getSprite(i);
            if (x >= sprite.getX() && x < sprite.getX() + 8 && y >= sprite.getY() && y < sprite.getY() + 8) {
                return Optional.of(sprite);
            }
        }
        return Optional.empty();
    }

    private boolean shouldWaitCpu() {
        long cpuCycles = cpu.getCyclesSynchronized();
        return this.cycles >= cpuCycles * 3;
    }

    public byte[] getCharacterPattern(int table, int i) {
        checkArgument(table >= 0 && table < 2);
        checkArgument(i >= 0 && i < 256);
        int from = table * 0x1000 + i * 0x10;
        return this.characterRom.getRange(from, from + 16);
    }

    /**
     *
     * @param x
     * @param y
     * @return 0 or 1
     */
    private int getScreen(int x, int y) {
        checkArgument(x >= 0 && x < 2 * WIDTH);
        checkArgument(y >= 0 && y < 2 * HEIGHT);
        if (y < HEIGHT) {
            if (x < WIDTH) {
                return 0;
            } else {
                return mirroring == Mirroring.VERTICAL ? 1 : 0;
            }
        } else {
            if (x < WIDTH) {
                return mirroring == Mirroring.VERTICAL ? 0 : 1;
            } else {
                return 1;
            }
        }
    }

    private Color getBackgroundColor(int x, int y) {
        checkArgument(x >= 0 && x < 2 * WIDTH);
        checkArgument(y >= 0 && y < 2 * HEIGHT);
        int screen = getScreen(x, y);
        int cell = getCell(x % WIDTH, y % HEIGHT);
        int character = getCharacter(screen, cell);
        int bgPatternTable = getBackgroundPatternTable();
        byte[] pattern = getCharacterPattern(bgPatternTable, character);
        int palette = getPalette(screen, cell);
        int color = getColorInPattern(x % 8, y % 8, pattern);
        int colorIndex = getColorIndex(palette, color);
        return Palette.get(colorIndex);
    }

    /**
     *
     * @param x
     * @param y
     * @param pattern
     * @return 0-3
     */
    private int getColorInPattern(int x, int y, byte[] pattern) {
        checkArgument(x >= 0 && x < 8);
        checkArgument(y >= 0 && y < 8);
        return (BinaryUtil.getBit(pattern[y], 7 - x) ? 1 : 0)
                + (BinaryUtil.getBit(pattern[y + 8], 7 - x) ? 1 : 0) * 2;
    }

    /**
     *
     * @param x
     * @param y
     * @return 0-959
     */
    private int getCell(int x, int y) {
        checkArgument(x >= 0 && x < WIDTH);
        checkArgument(y >= 0 && y < HEIGHT);
        int cellY = y / 8;
        int cellX = x / 8;
        return cellY * 32 + cellX;
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
        checkArgument(nameTable == 0 || nameTable == 1);
        checkArgument(cell >= 0 && cell < 960);
        return Byte.toUnsignedInt(nametables.get(nameTable * 0x400 + cell));
    }

    /**
     * @param palette 0-7
     * @param i 0-3
     * @return 0-63
     */
    public int getColorIndex(int palette, int i) {
        checkArgument(palette >= 0 && palette < 8);
        checkArgument(i >= 0 && i < 4);
        int offset = (i != 0) ? palette * 4 + i : 0;
        return paletteRam.get(offset);
    }

    public boolean isCharacterRomAvailable() {
        return characterRom != null;
    }

}
