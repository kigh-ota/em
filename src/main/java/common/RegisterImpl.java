package common;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

public class RegisterImpl implements Register {
    private int value;
    private final int width;

    public RegisterImpl(int value, int width) {
        this.value = value;
        Preconditions.checkArgument(width >= 1 && width <= 31);
        this.width = width;
    }

    // 桁溢れしたらtrue
    public boolean increment() {
        value++;
        return checkOverflow();
    }

    public boolean decrement() {
        if (value == 0) {
            value = maxValue();
            return true;
        }
        value--;
        return false;
    }

    @VisibleForTesting
    int maxValue() {
        return (1 << width) - 1;
    }

    public int get() {
        return value;
    }

    public void set(int value) {
        this.value = value;
    }

    public void set(RegisterImpl reg) {
        set(reg.get());
    }

    // 桁溢れしたらtrue
    public boolean add(int addend) {
        value += addend;
        return checkOverflow();
    }

    @Override
    public boolean subtract(byte subtrahend) {
        throw new UnsupportedOperationException();
    }

    public boolean add(RegisterImpl reg) {
        return add(reg.get());
    }

    public String toBinaryString() {
        return BinaryUtil.toBinaryString(value, width);
    }

    private boolean checkOverflow() {
        if (value >= (1 << width)) {
            value -= (1 << width);
            return true;
        }
        return false;
    }

    public boolean getBit(int bit) {
        return BinaryUtil.getBit(value, bit);
    }

    @Override
    public void setBit(boolean value, int bit) {
        throw new UnsupportedOperationException();
    }
}
