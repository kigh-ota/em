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

    public static boolean getBit(int value, int bit) {
        checkArgument(bit >= 0 && bit < 32);
        return (value & (1 << bit)) != 0;
    }

    public static boolean getBit(byte value, int bit) {
        checkArgument(bit >= 0 && bit < 8);
        return (value & (1 << bit)) != 0;
    }

    public static Pair<Byte, Boolean> add(byte augend, byte addend) {
        int value = Byte.toUnsignedInt(augend) + Byte.toUnsignedInt(addend);
        boolean carry = value > 255;
        return Tuples.pair((byte)(value % 256), carry);
    }

    public static int getAddress(byte lower, byte upper) {
        return (Byte.toUnsignedInt(upper) << 8) + Byte.toUnsignedInt(lower);
    }
}
