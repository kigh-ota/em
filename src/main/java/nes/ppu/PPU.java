package nes.ppu;

import common.BinaryUtil;
import common.ByteArrayMemory;
import common.ByteRegister;
import common.MemoryByte;
import lombok.Getter;
import lombok.Setter;
import nes.cpu.CPU;
import nes.screen.InfoScreen;
import nes.screen.MainScreen;
import nes.screen.MainScreenData;
import nes.screen.ScreenData;

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

    private int scanX;
    private int scanY;

    @Setter
    private CPU cpu;

    private MainScreen mainScreen;
    private InfoScreen infoScreen;

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
    public final MaskRegister regPPUMASK = new MaskRegister((byte)0);
    public final StatusRegister regPPUSTATUS; // $2002
    public final MemoryByte regOAMADDR = new ByteRegister((byte)0);
    public final MemoryByte regOAMDATA; // $2004
    public final ScrollRegister regPPUSCROLL; // $2005
    public final AddressRegister regPPUADDR; // $2006
    public final MemoryByte regPPUDATA; // $2007

    @Getter
    private final Mirroring mirroring;

    public PPU(ByteArrayMemory characterRom, Mirroring mirroring, MainScreen mainScreen, InfoScreen infoScreen) {
        memoryMapper = new MemoryMapper(this);
        this.characterRom = characterRom;
        nametables = new ByteArrayMemory(new byte[NAMETABLE_MEMORY_SIZE]);
        paletteRam = new ByteArrayMemory(new byte[PALETTE_RAM_SIZE]);
        regPPUCTRL = new ControlRegister();
        regPPUSTATUS = new StatusRegister(this);
        regOAMDATA = new OAMDataRegister(oam, regOAMADDR);
        regPPUSCROLL = new ScrollRegister(this);
        regPPUADDR = new AddressRegister(this);
        regPPUDATA = new DataRegister(this);

        this.mirroring = mirroring;

        this.mainScreen = mainScreen;
        this.infoScreen = infoScreen;
    }

    public void reset() {
        checkNotNull(mainScreen);
        mainScreen.init();
        infoScreen.init();

        checkNotNull(cpu);

        cycles = 0L;
        frames = 0L;
    }

    public void runStep() {

        // TODO NEXT draw dot by dot
//            cycles++;

        if (scanY == 0) {
            drawFrame();
            drawInfoScreen();

            cycles += (frames % 2 == 0) ? HEIGHT * 341 : HEIGHT * 341 - 1;
            scanY += HEIGHT;
            frames++;
        } else {
            if (scanX == 1 && scanY == 241) {
                regPPUSTATUS.setVblankBit(true);
                if (regPPUCTRL.getBit(7)) {
                    cpu.reserveNMI();
                }
            } else if (scanX == 1 && scanY == 261) {
                regPPUSTATUS.setVblankBit(false);
                regPPUSTATUS.setSprite0Hit(false);
            }

            cycles++;
            scanX++;
            if (scanX == 341) {
                scanY++;
                scanX = 0;
                if (scanY == 262) {
                    scanY = 0;
                }
            }
        }
    }

    @Override
    public void run() {
        reset();
        while (true) {
            if (shouldWaitCpu()) {
                return;
            }
            runStep();
        }
    }

    MainScreenData mainScreenData = new MainScreenData();

    private void drawFrame() {
        mainScreenData.clear();
        if (isCharacterRomAvailable()) {
            final int scrollX = regPPUSCROLL.getX();
            final int scrollY = regPPUSCROLL.getY();
            IntStream.range(0, HEIGHT).forEach(y -> {
                setLineData(y, scrollX, scrollY);
            });
        }
        mainScreen.refresh(mainScreenData);
    }

    ScreenData infoScreenData = new ScreenData(InfoScreen.WIDTH, InfoScreen.HEIGHT);

    private void drawInfoScreen() {
        infoScreenData.clear();
        // nametables
        if (isCharacterRomAvailable()) {
            for (int nametable = 0; nametable < 2; nametable++) {
                for (int tile = 0; tile < 256; tile++) {
                    final int x0 = 8 * (nametable * 16 + (tile % 16));
                    final int y0 = 8 * (tile / 16);
                    byte[] characterPattern = getCharacterPattern(nametable, tile);
                    for (int x = 0; x < 8; x++) {
                        for (int y = 0; y < 8; y++) {
                            int color = getColorInPattern(x, y, characterPattern);
                            infoScreenData.set(Palette.get(color), x0 + x, y0 + y);
                        }
                    }
                }
            }
        }
        // BG/Sprite palette
        for (int palette = 0; palette < 8; palette++) {
            for (int i = 0; i < 4; i++) {
                Color c = Palette.get(getColorIndex(palette, i));
                final int x0 = 8 * (4 * palette + i);
                final int y0 = 8 * 16;
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        infoScreenData.set(c, x0 + x, y0 + y);
                    }
                }
            }
        }
        // Sprites
        final int spritePatternTable = getSpritePatternTable();
        for (int sprite = 0; sprite < 64; sprite++) {
            final int x0 = 8 * (sprite % 32);
            final int y0 = 8 * (16 + 2 + (sprite / 32));
            byte[] pattern = getCharacterPattern(spritePatternTable, oam.getTileIndex(sprite));
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    int color = getColorInPattern(x, y, pattern);
                    int colorIndex = getColorIndex(oam.getPalette(sprite), color);
                    infoScreenData.set(Palette.get(colorIndex), x0 + x, y0 + y);
                }
            }
        }

        infoScreen.refresh(infoScreenData);

    }

    private void setLineData(int y, int scrollX, int scrollY) {
        IntStream.range(0, WIDTH).forEach(x -> {
            getColorAt(x, y, scrollX, scrollY).ifPresent(c -> {
                mainScreenData.set(c, x, y);
            });
        });
    }

    private Optional<Color> getColorAt(int x, int y, int scrollX, int scrollY) {
        return findTopSpriteNumber(x, y).map(sprite -> {
            int spritePatternTable = getSpritePatternTable();
            byte[] pattern = getCharacterPattern(spritePatternTable, oam.getTileIndex(sprite));

            boolean flippedHorizontally = oam.isFlippedHorizontally(sprite);
            boolean flippedVertically = oam.isFlippedVertically(sprite);
            int patternX = flippedHorizontally ? 7 - (x - oam.getX(sprite)) : x - oam.getX(sprite);
            int patternY = flippedVertically ? 7 - (y - oam.getY(sprite)) : y - oam.getY(sprite);
            int color = getColorInPattern(patternX, patternY, pattern);
            int colorIndex = getColorIndex(oam.getPalette(sprite), color);
            return Optional.of(Palette.get(colorIndex));
        }).orElseGet(() -> getBackgroundColor(x + scrollX, y + scrollY));
    }

    private Optional<Integer> findTopSpriteNumber(int x, int y) {
        if (!regPPUMASK.enableSprites()) {
            return Optional.empty();
        }
        checkArgument(x >= 0 && x < WIDTH);
        checkArgument(y >= 0 && y < HEIGHT);
        checkArgument(regPPUCTRL.getSpriteSize() == ControlRegister.SpriteSize.EIGHT_BY_EIGHT); // TODO 8x16 sprite
        for (int n = 0; n < 64; n++) {
            int spriteX = oam.getX(n);
            int spriteY = oam.getY(n);
            if (x >= spriteX && x < spriteX + 8 && y >= spriteY && y < spriteY + 8) {
                if (n == 0) {
                    regPPUSTATUS.setSprite0Hit(true);
                }
                return Optional.of(n);
            }
        }
        return Optional.empty();
    }

    private boolean shouldWaitCpu() {
        long cpuCycles = cpu.getCyclesSynchronized();
        return this.cycles >= cpuCycles * 3;
    }

    /**
     *
     * @param table 0-1
     * @param i 0-255
     * @return
     */
    private byte[] getCharacterPattern(int table, int i) {
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

    private Optional<Color> getBackgroundColor(int x, int y) {
        if (!regPPUMASK.enableBackground()) {
            return Optional.empty();
        }
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
        return Optional.of(Palette.get(colorIndex));
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
    private int getPalette(int nameTable, int cell) {
        int base = nameTable * 0x400 + ATTRIBUTE_TABLE_OFFSET;
        int cellRow = cell / 32; // 0-29
        int cellCol = cell % 32; // 0-31
        int attributeRow = cellRow / 4; // 0-7
        int attributeCol = cellCol / 4; // 0-7
        boolean isUpper = cellRow % 4 < 2;
        boolean isLeft = cellCol % 4 < 2;

        byte attribute = nametables.get(base + attributeRow * 8 + attributeCol);
        int shift = (isUpper ? 0 : 4) + (isLeft ? 0 : 2);
        return (Byte.toUnsignedInt(attribute) >> shift) & 3;
    }

    private int getBackgroundPatternTable() {
        return regPPUCTRL.getBackgroundPatternTable();
    }

    private int getSpritePatternTable() {
        return regPPUCTRL.getSpritePatternTable();
    }

    /**
     *
     * @param nameTable 0-1
     * @param cell 0-959
     * @return 0-255
     */
    private int getCharacter(int nameTable, int cell) {
        checkArgument(nameTable == 0 || nameTable == 1);
        checkArgument(cell >= 0 && cell < 960);
        return Byte.toUnsignedInt(nametables.get(nameTable * 0x400 + cell));
    }

    /**
     * @param palette 0-7
     * @param i 0-3
     * @return 0-63
     */
    private int getColorIndex(int palette, int i) {
        checkArgument(palette >= 0 && palette < 8);
        checkArgument(i >= 0 && i < 4);
        int offset = (i != 0) ? palette * 4 + i : 0;
        return paletteRam.get(offset);
    }

    private boolean isCharacterRomAvailable() {
        return characterRom != null;
    }

}
