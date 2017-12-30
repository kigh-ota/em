package nes.apu;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import nes.cpu.CPU;

import java.util.List;

import static nes.apu.FrameCounter.FrameCounterMode.FIVE_STEP;
import static nes.apu.FrameCounter.FrameCounterMode.FOUR_STEP;

class FrameCounter {

    @Setter
    private CPU cpu;

    private long cpuCycle;

    void reset() {
        cpuCycle = 0;
    }

    // TODO side effects https://wiki.nesdev.com/w/index.php/APU_Frame_Counter
    // TODO frame interrupt

    void clock() {
        cpuCycle++;
    }

    boolean isQuarterFrame() {
        FrameCounterMode frameCounterMode = getFrameCounterMode();
        int frame = (int)(cpuCycle % frameCounterMode.getCycles());
        return frameCounterMode.getQuarterFrames().contains(frame);
    }

    boolean isHalfFrame() {
        FrameCounterMode frameCounterMode = getFrameCounterMode();
        int frame = (int)(cpuCycle % frameCounterMode.getCycles());
        return frameCounterMode.getHalfFrames().contains(frame);
    }

    enum FrameCounterMode {
        FOUR_STEP(
                29830,
                ImmutableList.of(7457, 14913, 22371, 29829),
                ImmutableList.of(14913, 29829)),
        FIVE_STEP(
                37282,
                ImmutableList.of(7457, 14913, 22371, 37281),
                ImmutableList.of(14913, 37281));

        @Getter
        private final int cycles;

        @Getter
        private final List<Integer> quarterFrames;

        @Getter
        private final List<Integer> halfFrames;

        FrameCounterMode(int cycles, List<Integer> quarterFrames, List<Integer> halfFrames) {
            this.cycles = cycles;
            this.quarterFrames = quarterFrames;
            this.halfFrames = halfFrames;
        }
    }

    FrameCounterMode getFrameCounterMode() {
        return cpu.regJOY2.getBit(7) ? FIVE_STEP : FOUR_STEP;
    }

}
