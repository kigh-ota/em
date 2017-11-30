package nes.screen;

import common.BinaryUtil;
import nes.cpu._6502;
import nes.ppu.ControlRegister;
import nes.ppu.Mirroring;
import nes.ppu.PPU;
import nes.ppu.Sprite;

import java.awt.*;
import java.awt.image.BufferStrategy;

import static com.google.common.base.Preconditions.checkArgument;
import static nes.ppu.Mirroring.VERTICAL;

public class Window extends Canvas implements Runnable {
    private final PPU ppu;
    private final _6502 cpu;

    //ゲームのメインループスレッド
    Thread gameLoop;
    //BufferStrategy用
    BufferStrategy bstrategy;

    private static final int TILE_SIZE = 8;
    private static final int NUM_PATTERNS = 256;

    private static final int CELL_NUM_X = 32;
    private static final int CELL_NUM_Y = 30;

    private static final int MAIN_WIDTH = CELL_NUM_X * TILE_SIZE;
    private static final int MAIN_HEIGHT = CELL_NUM_Y * TILE_SIZE;
    private static final int MAIN_OFFSET_X = 0;
    private static final int MAIN_OFFSET_Y = TILE_SIZE * 16;

    private static final int PALETTE_OFFSET_X = TILE_SIZE * 16;
    private static final int PALETTE_OFFSET_Y = 0;

    //画面サイズ
    private static final int WIDTH = MAIN_WIDTH * 2; //2 * NUM_PATTERNS * TILE_SIZE;
    private static final int HEIGHT = 16 * TILE_SIZE + MAIN_HEIGHT * 2;

    // Palette (2C02)
    // https://wiki.nesdev.com/w/index.php/PPU_programmer_reference#Palettes
    private static final Color[] PALETTE = {
            new Color(96, 96, 96),
            new Color(0, 32, 128),
            new Color(0, 0, 192),
            new Color(96, 64, 192),
            new Color(128, 0, 96),
            new Color(160, 0, 96),
            new Color(160, 32, 0),
            new Color(128, 64, 0),
            new Color(96, 64, 0),
            new Color(32, 64, 0),
            new Color(0, 96, 32),
            new Color(0, 128, 0),
            new Color(0, 64, 64),
            new Color(0, 0, 0),
            new Color(0, 0, 0),
            new Color(0, 0, 0),
            new Color(160, 160, 160),
            new Color(0, 96, 192),
            new Color(0, 64, 224),
            new Color(128, 0, 224),
            new Color(160, 0, 224),
            new Color(224, 0, 128),
            new Color(224, 0, 0),
            new Color(192, 96, 0),
            new Color(128, 96, 0),
            new Color(32, 128, 0),
            new Color(0, 128, 0),
            new Color(0, 160, 96),
            new Color(0, 128, 128),
            new Color(0, 0, 0),
            new Color(0, 0, 0),
            new Color(0, 0, 0),
            new Color(224, 224, 224),
            new Color(96, 160, 224),
            new Color(128, 128, 224),
            new Color(192, 96, 224),
            new Color(224, 0, 224),
            new Color(224, 96, 224),
            new Color(224, 128, 0),
            new Color(224, 160, 0),
            new Color(192, 192, 0),
            new Color(96, 192, 0),
            new Color(0, 224, 0),
            new Color(64, 224, 192),
            new Color(0, 224, 224),
            new Color(0, 0, 0),
            new Color(0, 0, 0),
            new Color(0, 0, 0),
            new Color(224, 224, 224),
            new Color(160, 192, 224),
            new Color(192, 160, 224),
            new Color(224, 160, 224),
            new Color(224, 128, 224),
            new Color(224, 160, 160),
            new Color(224, 192, 128),
            new Color(224, 224, 64),
            new Color(224, 224, 96),
            new Color(160, 224, 64),
            new Color(128, 224, 96),
            new Color(64, 224, 192),
            new Color(128, 192, 224),
            new Color(0, 0, 0),
            new Color(0, 0, 0),
            new Color(0, 0, 0),
    };

    public Window(PPU ppu, _6502 cpu){
        this.ppu = ppu;
        this.cpu = cpu;

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
    }

    public void start(){
        //BufferedStrategy設定
        //バッファの数2つ(ダブルバッファリング)
        createBufferStrategy(2);
        //CanvasからBufferStrategy取得
        bstrategy = getBufferStrategy();
        //描画を自前でやる
        this.setIgnoreRepaint(true);

        //メインループ用のスレッドを作成して実行
        gameLoop = new Thread(this);
        gameLoop.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Graphics g = this.getGraphics();

                // Pattern tables
                for (int table = 0; table < 2; table++) {
                    for (int pattern = 0; pattern < NUM_PATTERNS; pattern++) {
                        byte[] character = ppu.getCharacterPattern(table, pattern);
                        int tileX = pattern % 16;
                        int tileY = pattern / 16;
                        drawTile(character, table * TILE_SIZE * NUM_PATTERNS + tileX * TILE_SIZE, tileY * TILE_SIZE, g);
                    }
                }

                // Background Palette
                for (int palette = 0; palette < 4; palette++) {
                    for (int i = 0; i < 4; i++) {
                        Color color = PALETTE[ppu.getColor(palette, i)];
                        g.setColor(color);
                        int x = PALETTE_OFFSET_X + i * 4;
                        int y = PALETTE_OFFSET_Y + palette * 4;
                        g.fillRect(x, y, 4, 4);
                    }
                }

                // Main: background
                Mirroring mirroring = ppu.getMirroring();
                int backgroundPatternTable = ppu.getBackgroundPatternTable();
                for (int nameTable = 0; nameTable < 2; nameTable++) {
                    int baseX = (nameTable == 0) ? MAIN_OFFSET_X : MAIN_OFFSET_X + MAIN_WIDTH;
                    int baseY = (nameTable == 0) ? MAIN_OFFSET_Y : MAIN_OFFSET_Y + MAIN_HEIGHT;
                    int baseMirrorX = (nameTable == 0) ?
                            (mirroring == VERTICAL ? baseX : baseX + MAIN_WIDTH) :
                            (mirroring == VERTICAL ? baseX : baseX - MAIN_WIDTH);
                    int baseMirrorY = (nameTable == 0) ?
                            (mirroring == VERTICAL ? baseY + MAIN_HEIGHT : baseY) :
                            (mirroring == VERTICAL ? baseY - MAIN_HEIGHT : baseY);

                    for (int cell = 0; cell < CELL_NUM_X * CELL_NUM_Y; cell++) {
                        int cellX = cell % CELL_NUM_X;
                        int cellY = cell / CELL_NUM_X;
                        byte[] character = ppu.getCharacterPattern(backgroundPatternTable, ppu.getCharacter(nameTable, cell));
                        int palette = ppu.getPalette(nameTable, cell);

                        int x0 = baseX + TILE_SIZE * cellX;
                        int y0 = baseY + TILE_SIZE * cellY;
                        drawTileWithPalette(character, x0, y0, palette, g);

                        int mirrorX0 = baseMirrorX + TILE_SIZE * cellX;
                        int mirrorY0 = baseMirrorY + TILE_SIZE * cellY;
                        drawTileWithPalette(character, mirrorX0, mirrorY0, palette, g);
                    }
                }

                int scrollX = ppu.regPPUSCROLL.getX();
                int scrollY = ppu.regPPUSCROLL.getY();

                // Sprites
                int spritePatternTable = ppu.getSpritePatternTable();
                checkArgument(ppu.regPPUCTRL.getSpriteSize() == ControlRegister.SpriteSize.EIGHT_BY_EIGHT);
                for (int n = 63; n >= 0; n--) {
                    Sprite sprite = ppu.oam.getSprite(n);
                    byte[] character = ppu.getCharacterPattern(spritePatternTable, sprite.getTileIndex());// TODO 8x16 sprite
                    int x = MAIN_OFFSET_X + scrollX + sprite.getX();
                    int y = MAIN_OFFSET_Y + scrollY + sprite.getY();
                    drawTileWithPalette(character, x, y, sprite.getAttributes().getPalette(), g);
                }

                // scroll position
                g.setColor(Color.YELLOW);
                g.drawRect(
                        MAIN_OFFSET_X + scrollX - 1,
                        MAIN_OFFSET_Y + scrollY - 1,
                        MAIN_WIDTH + 1,
                        MAIN_HEIGHT + 1);

                // show fps
                frames++;
                g.setColor(Color.WHITE);
                g.fillRect(300, 0, 100, 20);
                g.setColor(Color.BLACK);
                g.drawString(String.format("fps=%d;%d", updateFps(), cpu.getCycles()), 300, 20);

                ppu.regPPUSTATUS.setVblankBit(true);
                if (ppu.regPPUCTRL.getBit(7)) {
                    cpu.reserveNMI();
                }
                Thread.sleep(1/10*1000);    //1/60秒スリープ
                ppu.regPPUSTATUS.setVblankBit(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private long frames = 0;
    private Long previousTime = null;
    private long previousFrames = 0;
    private int fps = 0;

    private int updateFps() {
        if (previousTime == null) {
            previousTime = System.nanoTime();
        }
        long now = System.nanoTime();
        long window = now - previousTime;
        if (window >= 1000000000) {
            fps = (int)((frames - previousFrames) * 1000000000 / window);
            previousFrames = frames;
            previousTime = now;
        }
        return fps;
    }

    private void drawTile(byte[] data, int x0, int y0, Graphics g) {
        for (int offsetY = 0; offsetY < TILE_SIZE; offsetY++) {
            for (int offsetX = 0; offsetX < TILE_SIZE; offsetX++) {
                int color = (BinaryUtil.getBit(data[offsetY], 7 - offsetX) ? 1 : 0)
                        + (BinaryUtil.getBit(data[offsetY + 8], 7 - offsetX) ? 1 : 0) * 2;
                g.setColor(new Color(color * 255 / 3, color * 255 / 3, color * 255 / 3));
                g.drawLine(x0 + offsetX, y0 + offsetY, x0 + offsetX, y0 + offsetY);
            }
        }
    }

    /**
     * @param data
     * @param x0
     * @param y0
     * @param palette (palette) 0-3
     * @param g
     */
    private void drawTileWithPalette(byte[] data, int x0, int y0, int palette, Graphics g) {
        for (int offsetY = 0; offsetY < TILE_SIZE; offsetY++) {
            for (int offsetX = 0; offsetX < TILE_SIZE; offsetX++) {
                int color = (BinaryUtil.getBit(data[offsetY], 7 - offsetX) ? 1 : 0)
                        + (BinaryUtil.getBit(data[offsetY + 8], 7 - offsetX) ? 1 : 0) * 2;
                g.setColor(PALETTE[ppu.getColor(palette, color)]);
                g.drawLine(x0 + offsetX, y0 + offsetY, x0 + offsetX, y0 + offsetY);
            }
        }
    }
}
