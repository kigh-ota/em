package nes.cpu;

import common.BinaryUtil;
import common.ByteRegisterImpl;
import nes.Controller;

import java.util.HashMap;
import java.util.Map;

import static nes.Controller.Button.*;

public class JoystickRegister extends ByteRegisterImpl {
    private Controller controller;
    private boolean strobe;
    private Map<Controller.Button, Boolean> pressed;
    private Controller.Button readNext;

    public JoystickRegister(Controller controller) {
        super((byte)0);
        strobe = false;
        pressed = new HashMap<>();
        pressed.put(null, true);
        readNext = A;
        this.controller = controller;
    }

    @Override
    public void set(byte value) {
        strobe = BinaryUtil.getBit(value, 0);
        if (!strobe) {
            updateState();
        }
        readNext = A;
    }

    @Override
    public byte get() {
        if (strobe) {
            updateState();
            return pressed.get(A) ? (byte)1 : (byte)0;  // TODO expansion and microphone
        } else {
            byte ret = pressed.get(readNext) ? (byte)1 : (byte)0;
            readNext = nextReadNext();
            return ret;
        }
    }

    private void updateState() {
        pressed.putAll(controller.getPressed());
    }

    private Controller.Button nextReadNext() {
        if (readNext == null) {
            return null;
        }
        switch (readNext) {
            case A:
                return B;
            case B:
                return SELECT;
            case SELECT:
                return START;
            case START:
                return UP;
            case UP:
                return DOWN;
            case DOWN:
                return LEFT;
            case LEFT:
                return RIGHT;
            case RIGHT:
                return null;
        }
        return null;
    }

}
