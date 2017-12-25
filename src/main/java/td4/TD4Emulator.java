package td4;

import common.BinaryUtil;
import common.IntegerRegister;

import java.util.Timer;

import static com.google.common.base.Preconditions.checkNotNull;

public class TD4Emulator extends java.util.TimerTask {
    static private final int ADDRESS_SPACE_SIZE = 16;
    static private final int WIDTH = 4;

    public enum ClockType {
        SLOW(1000L),
        FAST(100L);
        private long milliSecond;

        private ClockType(long ms) {
            milliSecond = ms;
        }
    }

    // http://www.itolab.com/wp-content/uploads/td4-sample.pdf
    static private final byte SAMPLE_PROGRAM_TIMER[] = {
            (byte)0b10110000,   // OUT 0000
            (byte)0b00000001,   // ADD A,0001
            (byte)0b11100000,   // JNC 0000
            (byte)0b00111011,   // MOV A,1011
            (byte)0b00000001,   // ADD A,0001
            (byte)0b11100100,   // JNC 0100
            (byte)0b10111000,   // OUT 1000
            (byte)0b11110111,   // JMP 0111
            (byte)0b00000000,
            (byte)0b00000000,
            (byte)0b00000000,
            (byte)0b00000000,
            (byte)0b00000000,
            (byte)0b00000000,
            (byte)0b00000000,
            (byte)0b00000000
    };

    static private final byte SAMPLE_PROGRAM_MARQUEE[] = {
            (byte)0b10111000,   // OUT 1000
            (byte)0b10110100,   // OUT 0100
            (byte)0b10110010,   // OUT 0010
            (byte)0b10110001,   // OUT 0001
            (byte)0b11110100,   // JMP 0100
            (byte)0b00000000,
            (byte)0b00000000,
            (byte)0b00000000,
            (byte)0b00000000,
            (byte)0b00000000,
            (byte)0b00000000,
            (byte)0b00000000,
            (byte)0b00000000,
            (byte)0b00000000,
            (byte)0b00000000,
            (byte)0b00000000
    };

    private final IntegerRegister registerA;
    private final IntegerRegister registerB;
    private boolean carryFlag = false;
    private final IntegerRegister programCounter;
    private byte memory[];
    private final IntegerRegister inPort;
    private final IntegerRegister outPort;

    public TD4Emulator() {
        registerA = new IntegerRegister(0b0000, WIDTH);
        registerB = new IntegerRegister(0b0000, WIDTH);
        programCounter = new IntegerRegister(0b0000, WIDTH);
        inPort = new IntegerRegister(0b0000, WIDTH);
        outPort = new IntegerRegister(0b0000, WIDTH);

//        memory = SAMPLE_PROGRAM_MARQUEE;
        memory = SAMPLE_PROGRAM_TIMER;
    }

    @Override
    public void run() {
        decode();
    }

    public void start(ClockType clockType) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(this, 0, clockType.milliSecond);
    }

    private void decode() {
        System.out.print(BinaryUtil.toBinaryString(memory[programCounter.get()], 8));
        Instruction instruction = Instruction.parse(memory[programCounter.get()]);
        programCounter.increment();
        executeInstruction(instruction);
    }

    private void executeInstruction(Instruction instruction) {
        checkNotNull(instruction);
        checkNotNull(instruction.getOpcode());
        System.out.print(" " + instruction.getOpcode().toString());
        System.out.print("," + BinaryUtil.toBinaryString(instruction.getOperand(), WIDTH));
        System.out.print(" out=" + outPort.toBinaryString());
        System.out.println("");
        switch (instruction.getOpcode()) {
            case ADD_A_X:
                carryFlag = registerA.add(instruction.getOperand());
                break;
            case ADD_B_X:
                carryFlag = registerB.add(instruction.getOperand());
                break;
            case MOV_A_X:
                registerA.set(instruction.getOperand());
                break;
            case MOV_B_X:
                registerB.set(instruction.getOperand());
                break;
            case MOV_A_B:
                registerB.set(registerA.get());
                break;
            case MOV_B_A:
                registerA.set(registerB.get());
                break;
            case JMP_X:
                programCounter.set(instruction.getOperand());
                break;
            case JNC_X:
                if (!carryFlag) {
                    programCounter.set(instruction.getOperand());
                }
                break;
            case IN_A:
                registerA.set(inPort.get());
                break;
            case IN_B:
                registerB.set(inPort.get());
                break;
            case OUT_B:
                outPort.set(registerB.get());
                break;
            case OUT_X:
                outPort.set(instruction.getOperand());
                break;
        }
    }

}
