package it.enryold.quasarflow.models;

import co.paralleluniverse.strands.channels.Channels;

public class QSettings {

    private int bufferSize;
    private Channels.OverflowPolicy overflowPolicy;

    private QSettings(Builder builder) {
        bufferSize = builder.bufferSize;
        overflowPolicy = builder.overflowPolicy;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public Channels.OverflowPolicy getOverflowPolicy() {
        return overflowPolicy;
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public static QSettings test() {
        return new Builder().withBufferSize(100)
                .withOverflowPolicy(Channels.OverflowPolicy.BLOCK)
                .build();
    }

    public static QSettings standard() {
        return new Builder().withBufferSize(1_000)
                .withOverflowPolicy(Channels.OverflowPolicy.BLOCK)
                .build();
    }

    public static QSettings highLoad() {
        return new Builder().withBufferSize(1_000_000)
                .withOverflowPolicy(Channels.OverflowPolicy.BLOCK)
                .build();
    }


    public static final class Builder {
        private int bufferSize;
        private Channels.OverflowPolicy overflowPolicy;

        private Builder() {
        }

        public Builder withBufferSize(int val) {
            bufferSize = val;
            return this;
        }

        public Builder withOverflowPolicy(Channels.OverflowPolicy val) {
            overflowPolicy = val;
            return this;
        }

        public QSettings build() {
            return new QSettings(this);
        }
    }
}
