package nes;

import nes.apu.APU;
import nes.cpu.CPU;
import nes.ppu.PPU;
import nes.screen.InfoScreen;
import nes.screen.MainScreen;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class NesEmulator {
    public void start(String romFileName) {
        NesData nesData = loadRom(romFileName);

        Controller controller1 = new Controller();

        MainScreen mainScreen = new MainScreen(controller1);
        InfoScreen infoScreen = new InfoScreen();

        PPU ppu = new PPU(nesData.characterRom, nesData.mirroring, mainScreen, infoScreen);
        APU apu = new APU();
        CPU cpu = new CPU(ppu, apu, nesData.programRom, controller1);
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
