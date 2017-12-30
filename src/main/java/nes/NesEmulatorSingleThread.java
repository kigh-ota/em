package nes;

import nes.apu.APU;
import nes.cpu.CPU;
import nes.ppu.PPU;
import nes.screen.InfoScreen;
import nes.screen.MainScreen;

import javax.sound.sampled.LineUnavailableException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class NesEmulatorSingleThread {
    public void start(String romFileName) {
        NesData nesData = loadRom(romFileName);

        Controller controller1 = new Controller();
        MainScreen mainScreen = new MainScreen(controller1);
        InfoScreen infoScreen = new InfoScreen();
        PPU ppu = new PPU(nesData.characterRom, nesData.mirroring, mainScreen, infoScreen);
        APU apu = new APU();
        CPU cpu = new CPU(ppu, apu, nesData.programRom, controller1);
        ppu.setCpu(cpu);
        apu.setCpu(cpu);

        cpu.reset();
        ppu.reset();
        try {
            apu.reset();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        while (true) {
            long cpuCycleBefore = cpu.getCyclesSynchronized();
            cpu.runStep();
            long cpuCycleAfter = cpu.getCyclesSynchronized();
            for (int i = 0; i < cpuCycleAfter - cpuCycleBefore; i++) {
                ppu.runStep();
                ppu.runStep();
                ppu.runStep();
                apu.runStep();
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
