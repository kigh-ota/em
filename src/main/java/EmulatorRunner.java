import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import nes.NesEmulatorSingleThread;
import org.slf4j.LoggerFactory;

public class EmulatorRunner {
    public static void main(String args[]) {
        LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("nes");
        rootLogger.setLevel(Level.valueOf(System.getProperty("loglevel")));
//        TD4Emulator td4Simulator = new TD4Emulator();
//        td4Simulator.start(TD4Emulator.ClockType.FAST);
//        new NesEmulator().start();
        new NesEmulatorSingleThread().start(args[0]);
    }
}
