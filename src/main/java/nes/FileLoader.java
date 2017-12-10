package nes;

import common.BinaryUtil;
import common.ByteArrayMemory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
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

        programRomSize = data[4];
        characterRomSize = data[5] * 0x2000;
        flag6 = data[6];
        flag7 = data[7];
        programRamSize = data[8] == 0 ? 0x2000 : data[8] * 0x2000;
        flag9 = data[9];
        flag10 = data[10];

        log.info("Size of PRG ROM (in 16 KB units) = {}", data[4]);
        log.info("Size of CHR ROM (in 8 KB units) = {}", data[5]);
        log.info("Flags 6 = {}", BinaryUtil.toBinaryString(data[6], 8));
        log.info("Flags 7 = {}", BinaryUtil.toBinaryString(data[7], 8));
        log.info("Size of PRG RAM (in 8 KB units) = {}", data[8]);
        log.info("Flags 9 = {}", BinaryUtil.toBinaryString(data[9], 8));
        log.info("Flags 10 = {}", BinaryUtil.toBinaryString(data[10], 8));

        checkArgument(data[8] == 0 || data[8] == 1);

        boolean zeroFilled = IntStream.range(11, HEADER_LENGTH).allMatch(pos -> data[pos] == 0);
        return zeroFilled;
    }

    private boolean checkHeaderConstant(byte[] data) {
        return (data[0] == 0x4E && data[1] == 0x45 && data[2] == 0x53 && data[3] == 0x1A);
    }

    private NesData parseData(byte[] data) {
        NesData nesData = new NesData(flag6);
        int cursor = HEADER_LENGTH;
        if (programRomSize == 2) {
            nesData.programRom = new ByteArrayMemory(Arrays.copyOfRange(data, cursor, cursor + 0x8000));
            cursor += 0x8000;
        } else if (programRomSize == 1) {
            nesData.programRom = new ByteArrayMemory(new byte[0x8000]);
            for (int i = 0; i < 0x8000; i++) {
                nesData.programRom.set(data[cursor + (i % 0x4000)], i);
            }
            cursor += 0x4000;
        } else {
            throw new IllegalStateException();
        }
        if (characterRomSize > 0) {
            nesData.characterRom = new ByteArrayMemory(Arrays.copyOfRange(data, cursor, cursor + characterRomSize));
        }
        return nesData;
    }
}
