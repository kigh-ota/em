import nes.NesEmulator;

public class EmulatorRunner {
    public static void main(String args[]) {
//        TD4Emulator td4Simulator = new TD4Emulator();
//        td4Simulator.start(TD4Emulator.ClockType.FAST);
        NesEmulator emulator = new NesEmulator();
        emulator.start();
    }
}
