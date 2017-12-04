package nes;

import nes.cpu.CPU;
import nes.ppu.PPU;
import nes.screen.Window;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class NesEmulator {
    public void start() {
        NesData nesData = loadRom(System.getProperty("user.home") + "/sample1.nes");
//        NesData nesData = loadRom(System.getProperty("user.home") + "/color_test.nes");
//        NesData nesData = loadRom(System.getProperty("user.home") + "/sprite_ram.nes");

        PPU ppu = new PPU(nesData.characterRom, nesData.mirroring);
        CPU cpu = new CPU(ppu, nesData.programRom);

        startScreen(ppu, cpu);
        new Thread(ppu).start();
        new Thread(cpu).start();
    }

    private NesData loadRom(String romFileName) {
        InputStream in;
        try {
            in = new FileInputStream(romFileName);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found");
        }
        return new FileLoader().load(in);
    }

    private void startScreen(PPU ppu, CPU cpu) {
        //新しいフレーム(Window)を作成
        JFrame mainFrame = new JFrame();

        //Canvasクラスを継承したGameMainクラスのインスタンスを作成
        Window window = new Window(ppu, cpu);

        //フレームのタイトルを設定
        mainFrame.setTitle("NesEmulator");
        //Xボタン押下時の動作(終了)
        mainFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        //ウィンドウサイズの固定
        mainFrame.setResizable(false);
        //画面表示
        mainFrame.setVisible(true);

        //フレームにキャンバスを載せる
        mainFrame.getContentPane().add(window);

        //フレームサイズをキャンバスに合わせる
        mainFrame.pack();
        //フレームの表示位置を画面中央に置く
        mainFrame.setLocationRelativeTo(null);
        //ゲームのメイン処理スタート
        window.start();
    }
}
