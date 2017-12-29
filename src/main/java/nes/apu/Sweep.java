package nes.apu;

import lombok.Setter;

public class Sweep {
    private final Pulse pulse;

    private int divider;
    @Setter private int dividerReset;
    @Setter private boolean enabled;
    @Setter private boolean negateFlag;
    @Setter private boolean reloadFlag;
    @Setter private int shiftCount;
    private boolean mute;

    Sweep(Pulse pulse) {
        this.pulse = pulse;
    }

    void reset() {
        divider = 0;
        enabled = false;
        negateFlag = false;
        reloadFlag = false;
        shiftCount = 0;
        mute = false;
    }

    void clock() {
        if (divider == 0 && enabled) {
            // adjust period if the target period is in range
            int targetPeriod = calculateTargetPeriod();
            mute = targetPeriod >= 0x800;
            if (!mute) {
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
