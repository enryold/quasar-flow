package it.enryold.quasarflow.kinesis.firehose.consts;

public class PutRecordLimits {
    public static final double PUT_BATCH_LIMIT_MB = 4_000_000.0;
    public static final int PUT_BATCH_LIMIT_RECORDS = 500;
    public static final double PUT_LIMIT_MB = 999_999.0;
}
