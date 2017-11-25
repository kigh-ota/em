package td4;

import lombok.Value;

@Value
public class Instruction {
    Opcode opcode;
    byte operand;

    static Instruction parse(byte data) {
        Opcode opcode = Opcode.parse(data);
        byte operand = (byte)(Byte.toUnsignedInt(data) & 0b1111);
        return new Instruction(opcode, operand);
    }
}
