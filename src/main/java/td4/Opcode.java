package td4;

// 命令語：8ビット
// 命令セットの大きさ：16
public enum Opcode {
    ADD_A_X(0b0000),
    ADD_B_X(0b0101),
    MOV_A_X(0b0011),
    MOV_B_X(0b0111),
    MOV_A_B(0b0001),
    MOV_B_A(0b0100),
    JMP_X(0b1111),
    JNC_X(0b1110),
    IN_A(0b0010),
    IN_B(0b0110),
    OUT_B(0b1001),
    OUT_X(0b1011);
    //NOP(0b0000);

    private final byte value;

    private Opcode(int value) {
        this.value = (byte)value;
    }

    static Opcode parse(byte data) {
        byte op = (byte)(Byte.toUnsignedInt(data) >>> 4);
        for (Opcode opcode: Opcode.values()) {
            if (opcode.value == op) {
                return opcode;
            }
        }
        return null;
    }
}
