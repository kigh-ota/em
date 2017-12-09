package nes;

import nes.cpu.CPU;
import nes.ppu.PPU;
import nes.screen.MainScreen;
import nes.screen.MainScreenImpl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class NesEmulator {
    public void start() {
//        NesData nesData = loadRom(System.getProperty("user.home") + "/sample1.nes");
        NesData nesData = loadRom(System.getProperty("user.home") + "/color_test.nes");
//        NesData nesData = loadRom(System.getProperty("user.home") + "/sprite_ram.nes");

        Controller controller1 = new Controller();

        MainScreen mainScreen = new MainScreenImpl(controller1);

        PPU ppu = new PPU(nesData.characterRom, nesData.mirroring, mainScreen);
        CPU cpu = new CPU(ppu, nesData.programRom, controller1);
        ppu.setCpu(cpu);


//        startScreen(ppu, cpu);
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

    // TODO separate main and informational window
}
