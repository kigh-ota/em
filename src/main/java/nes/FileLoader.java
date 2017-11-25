package nes;

import common.ByteArrayMemory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.IntStream;

public class FileLoader {
    public static final int HEADER_LENGTH = 16;

    private int programRomSize;
    private int characterRomSize;
    private byte flag6;
    private byte flag7;
    private int programRamSize;
    private byte flag9;
    private byte flag10;

    public NesData load(InputStream in) {
        byte[] data = loadAll(in);
        parseHeader(data);
        return parseData(data);
    }

    private byte[] loadAll(InputStream in) {
        byte[] data = new byte[0];
        try {
            data = new byte[in.available()];
            in.read(data, 0, data.length);
        } catch (IOException e) {
        }
        return data;
    }

    private boolean parseHeader(byte[] data) {
        if (data.length < HEADER_LENGTH) {
            return false;
        }

        if (!checkHeaderConstant(data)) {
            return false;
        }

        programRomSize = data[4] * 0x4000;
        characterRomSize = data[5] * 0x2000;
        flag6 = data[6];
        flag7 = data[7];
        programRamSize = data[8] * 0x2000;
        flag9 = data[9];
        flag10 = data[10];

        boolean zeroFilled = IntStream.range(11, HEADER_LENGTH).allMatch(pos -> data[pos] == 0);
        return zeroFilled;
    }

    private boolean checkHeaderConstant(byte[] data) {
        return (data[0] == 0x4E && data[1] == 0x45 && data[2] == 0x53 && data[3] == 0x1A);
    }

    private NesData parseData(byte[] data) {
        NesData nesData = new NesData(flag6);
        int cursor = HEADER_LENGTH;
        nesData.programRom = new ByteArrayMemory(Arrays.copyOfRange(data, cursor, cursor + programRomSize));
        cursor += programRomSize;
        nesData.characterRom = new ByteArrayMemory(Arrays.copyOfRange(data, cursor, cursor + characterRomSize));
        return nesData;
    }
}
