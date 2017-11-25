package screen;

import common.BinaryUtil;
import nes.ppu.Mirroring;
import nes.ppu.PPU;

import java.awt.*;
import java.awt.image.BufferStrategy;

import static nes.ppu.Mirroring.VERTICAL;

public class Window extends Canvas implements Runnable {
    private final PPU ppu;

    //ゲームのメインループスレッド
    Thread gameLoop;
    //BufferStrategy用
    BufferStrategy bstrategy;

    private static final int TILE_SIZE = 8;
    private static final int TILE_NUM = 256;

    private static final int CELL_NUM_X = 32;
    private static final int CELL_NUM_Y = 30;

    private static final int MAIN_WIDTH = CELL_NUM_X * TILE_SIZE;
    private static final int MAIN_HEIGHT = CELL_NUM_Y * TILE_SIZE;
    private static final int MAIN_OFFSET_X = 0;
    private static final int MAIN_OFFSET_Y = TILE_SIZE * 16;

    private static final int PALETTE_OFFSET_X = TILE_SIZE * 16;
    private static final int PALETTE_OFFSET_Y = 0;

    //画面サイズ
    private static final int WIDTH = MAIN_WIDTH * 2; //2 * TILE_NUM * TILE_SIZE;
    private static final int HEIGHT = 16 * TILE_SIZE + MAIN_HEIGHT * 2;

    // Palette (2C02)
    // https://wiki.nesdev.com/w/index.php/PPU_programmer_reference#Palettes
    private static final Color[] palette = {
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

    public Window(PPU ppu){
        this.ppu = ppu;

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
                    for (int i = 0; i < TILE_NUM; i++) {
                        byte[] character = ppu.getCharacter(table, i);
                        int tileX = i % 16;
                        int tileY = i / 16;
                        drawTile(character, table * TILE_SIZE * TILE_NUM + tileX * TILE_SIZE, tileY * TILE_SIZE, g);
                    }
                }

                // Background Palette
                for (int p = 0; p < 4; p++) {
                    for (int i = 0; i < 4; i++) {
                        int offset = (i != 0) ? p * 4 + i : 0;
                        Color color = palette[ppu.paletteRam.at(offset).get()];
                        g.setColor(color);
                        int x = PALETTE_OFFSET_X + i * 4;
                        int y = PALETTE_OFFSET_Y + p * 4;
                        g.fillRect(x, y, 4, 4);
                    }
                }

                // Main: background
                Mirroring mirroring = ppu.getMirroring();
                // nametable 0
                for (int i = 0; i < CELL_NUM_X * CELL_NUM_Y; i++) {
                    int cellX = i % CELL_NUM_X;
                    int cellY = i / CELL_NUM_X;
                    byte[] character = ppu.getCharacter(0 /* FIXME use register */, Byte.toUnsignedInt(ppu.nametables.at(i).get()));
                    int x0 = MAIN_OFFSET_X + TILE_SIZE * cellX;
                    int y0 = MAIN_OFFSET_Y + TILE_SIZE * cellY;
                    drawTile(character, x0, y0, g); // TODO use palette
                    // mirror
                    int mirrorX0 = mirroring == VERTICAL ? x0 : x0 + MAIN_WIDTH;
                    int mirrorY0 = mirroring == VERTICAL ? y0 + MAIN_HEIGHT : y0;
                    drawTile(character, mirrorX0, mirrorY0, g); // TODO use palette
                }

                // nametable 1
                for (int i = 0; i < CELL_NUM_X * CELL_NUM_Y; i++) {
                    int cellX = i % CELL_NUM_X;
                    int cellY = i / CELL_NUM_X;
                    byte[] character = ppu.getCharacter(1 /* FIXME use register */, Byte.toUnsignedInt(ppu.nametables.at(i).get()));
                    int x0 = MAIN_OFFSET_X + MAIN_WIDTH + TILE_SIZE * cellX;
                    int y0 = MAIN_OFFSET_Y + MAIN_HEIGHT + TILE_SIZE * cellY;
                    drawTile(character, x0, y0, g); // TODO use palette
                    // mirror
                    int mirrorX0 = mirroring == VERTICAL ? x0 : x0 - MAIN_WIDTH;
                    int mirrorY0 = mirroring == VERTICAL ? y0 - MAIN_HEIGHT : y0;
                    drawTile(character, mirrorX0, mirrorY0, g); // TODO use palette
                }

                // scroll position
                g.setColor(Color.YELLOW);
                int scrollX = ppu.regPPUSCROLL.getX();
                int scrollY = ppu.regPPUSCROLL.getY();
                g.drawRect(
                        MAIN_OFFSET_X + scrollX - 1,
                        MAIN_OFFSET_Y + scrollY - 1,
                        MAIN_WIDTH + 1,
                        MAIN_HEIGHT + 1);

                Thread.sleep(1/60*1000);    //1/60秒スリープ
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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

    private void drawTileWithPalette(byte[] data, int x0, int y0, Graphics g) {
        for (int offsetY = 0; offsetY < TILE_SIZE; offsetY++) {
            for (int offsetX = 0; offsetX < TILE_SIZE; offsetX++) {
                int color = (BinaryUtil.getBit(data[offsetY], 7 - offsetX) ? 1 : 0)
                        + (BinaryUtil.getBit(data[offsetY + 8], 7 - offsetX) ? 1 : 0) * 2;
                // attribute table
                g.setColor(color);
                g.drawLine(x0 + offsetX, y0 + offsetY, x0 + offsetX, y0 + offsetY);
            }
        }
    }

    private Color getPaletteColor(int p, int i) {
        int offset = (i != 0) ? p * 4 + i : 0;
        return palette[ppu.paletteRam.at(offset).get()];
    }
}
