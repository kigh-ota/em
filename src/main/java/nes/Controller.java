package nes;

import lombok.Getter;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.awt.event.KeyEvent.*;
import static nes.Controller.Button.*;

public class Controller extends KeyAdapter {

    @Getter
    Map<Button, Boolean> pressed;

    public enum Button {
        A, B, SELECT, START, UP, DOWN, LEFT, RIGHT
    }

    public Controller() {
        pressed = new HashMap<>();
        for (Button button: Button.values()) {
            pressed.put(button, false);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        checkNotNull(pressed.replace(keyCodeToButton(e.getKeyCode()), true));
    }

    @Override
    public void keyReleased(KeyEvent e) {
        checkNotNull(pressed.replace(keyCodeToButton(e.getKeyCode()), false));
    }

    private Button keyCodeToButton(int keyCode) {
        switch (keyCode) {
            case VK_X:
                return A;
            case VK_Z:
                return B;
            case VK_A:
                return START;
            case VK_S:
                return SELECT;
            case VK_UP:
                return UP;
            case VK_DOWN:
                return DOWN;
            case VK_LEFT:
                return LEFT;
            case VK_RIGHT:
                return RIGHT;
        }
        throw new IllegalArgumentException();
    }
}


