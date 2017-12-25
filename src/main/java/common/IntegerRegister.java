package common;

import lombok.Getter;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * 符号なし整数値を保持するレジスタ
 * 使用可能なビット幅を指定可能
 */
public class IntegerRegister {
    private int value;
    private final int width;
    @Getter
    private final int maxValue;

    public IntegerRegister(int value, int width) {
        checkArgument(width >= 1 && width <= 31);
        this.value = value;
        this.width = width;
        this.maxValue = getMaxValue(width);
    }

    private int getMaxValue(int width) {
        int tmp = 0;
        for (int b = 0; b < width; b++) {
            tmp |= (1 << b);
        }
        return tmp;
    }

    public boolean increment() {
        if (value == maxValue) {
            value = 0;
            return true;
        }
        value++;
        return false;
    }

    public boolean decrement() {
        if (value == 0) {
            value = maxValue;
            return true;
        }
        value--;
        return false;
    }

    public int get() {
        return value;
    }

    public void set(int value) {
        checkArgument(value >= 0 && value <= maxValue);
        this.value = value;
    }

    public boolean add(int addend) {
        checkArgument(addend >= 0 && addend <= maxValue);
        value += addend;
        if (BinaryUtil.getBit(value, width)) {
            value = BinaryUtil.setBit(false, value, width);
            return true;
        }
        return false;
    }

    public String toBinaryString() {
        return BinaryUtil.toBinaryString(value, width);
    }

    public boolean getBit(int bit) {
        checkArgument(bit >= 0 && bit < width);
        return BinaryUtil.getBit(value, bit);
    }

    public void setBit(boolean flag, int bit) {
        checkArgument(bit >= 0 && bit < width);
        value = BinaryUtil.setBit(flag, value, bit);
    }
}
