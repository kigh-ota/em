package nes;

import nes.cpu.CPU;
import nes.ppu.PPU;
import nes.screen.MainScreen;
import nes.screen.MainScreenImpl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class NesEmulatorSingleThread {
    public void start() {
        NesData nesData = loadRom(System.getProperty("user.home") + "/sample1.nes");

        Controller controller1 = new Controller();
        MainScreen mainScreen = new MainScreenImpl(controller1);
        PPU ppu = new PPU(nesData.characterRom, nesData.mirroring, mainScreen);
        CPU cpu = new CPU(ppu, nesData.programRom, controller1);
        ppu.setCpu(cpu);

        cpu.reset();
        ppu.reset();
        while (true) {
            long cpuCycleBefore = cpu.getCyclesSynchronized();
            cpu.runStep();
            long cpuCycleAfter = cpu.getCyclesSynchronized();
            for (int i = 0; i < (cpuCycleAfter - cpuCycleBefore) * 3; i++) {
                ppu.runStep();
            }
        }

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
}
