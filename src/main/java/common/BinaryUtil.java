package common;

import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

import static com.google.common.base.Preconditions.checkArgument;

public class BinaryUtil {
    public static String toBinaryString(int value, int width) {
        return String.format(String.format("%%%ds", width), Integer.toBinaryString(value)).replace(' ', '0');
    }

    public static String toBinaryString(byte value, int width) {
        return toBinaryString(Byte.toUnsignedInt(value), width);
    }

    public static String toHexString(byte value) {
        return String.format("$%02x", Byte.toUnsignedInt(value));
    }

    public static String toHexString(int value) {
        return String.format("$%04x", value);
    }

    public static boolean getBit(int value, int bit) {
        checkArgument(bit >= 0 && bit < 32);
        return (value & (1 << bit)) != 0;
    }

    public static boolean getBit(byte value, int bit) {
        checkArgument(bit >= 0 && bit < 8);
        return (value & (1 << bit)) != 0;
    }

    public static byte setBit(boolean flag, byte value, int bit) {
        checkArgument(bit >= 0 && bit < 8);
        if (flag) {
            return value |= (1 << bit);
        } else {
            return value &= ~(1 << bit);
        }
    }

    public static Pair<Byte, Boolean> add(byte augend, byte addend) {
        int value = Byte.toUnsignedInt(augend) + Byte.toUnsignedInt(addend);
        boolean carry = value > 255;
        return Tuples.pair((byte)(value % 256), carry);
    }

    public static Pair<Byte, Boolean> subtract(byte minuend, byte subtrahend) {
        int value = Byte.toUnsignedInt(minuend) - Byte.toUnsignedInt(subtrahend);
        boolean carry = value < 0;
        return Tuples.pair((byte)(value % 256), carry);
    }

    public static int getAddress(byte lower, byte upper) {
        return (Byte.toUnsignedInt(upper) << 8) + Byte.toUnsignedInt(lower);
    }
}
