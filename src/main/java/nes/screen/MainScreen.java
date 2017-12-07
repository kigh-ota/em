package nes.screen;

public interface MainScreen {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 240;

    void init();

    void refresh(MainScreenData data);
}