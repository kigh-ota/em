package nes.apu;

import lombok.Getter;
import lombok.Setter;

public class Sweep {
    private final PulseChannel pulse;

    private int divider;
    @Setter private int dividerReset;
    @Setter private boolean enabled;
    @Setter private boolean negateFlag;
    @Setter private boolean reloadFlag;
    @Setter private int shiftCount;
    @Getter private boolean muting;

    Sweep(PulseChannel pulse) {
        this.pulse = pulse;
    }

    void reset() {
        divider = 0;
        enabled = false;
        negateFlag = false;
        reloadFlag = false;
        shiftCount = 0;
        muting = false;
    }

    void clock() {
        if (divider == 0 && enabled) {
            // adjust period if the target period is in range
            int targetPeriod = calculateTargetPeriod();
            muting = targetPeriod >= 0x800;
            if (!muting) {
                pulse.setTimerReset(targetPeriod);
            }
        }
        if (divider == 0 || reloadFlag) {
            divider = dividerReset;
            reloadFlag = false;
        } else {
            divider--;
        }
    }

    private int calculateTargetPeriod() {
        int base = pulse.getTimerReset();
        int diff = base >> shiftCount;
        return negateFlag ? base - diff : base + diff;
    }
}
