package nes.apu.channel;

import lombok.Getter;
import lombok.Setter;

public abstract class ChannelWithEnvelope extends ChannelWithLengthCounter {

    @Getter
    protected final Envelope envelope;

    @Setter
    @Getter
    protected int volume; // 0-15, also used as the envelope divider period

    @Setter
    protected boolean useConstantVolume; // constant volume or envelope

    ChannelWithEnvelope() {
        super();
        envelope = new Envelope(this);
        volume = 0;
        useConstantVolume = false;
    }

    @Override
    void reset() {
        super.reset();
        envelope.reset();
    }

    protected int getEnvelopedVolume() {
        return useConstantVolume ? volume : envelope.getDecayLevel();
    }

    // Envelope
    public void clockEnvelope() {
        envelope.clock();
    }
}
