package it.enryold.quasarflow.kinesis.streams.producer.hashproviders.utils;


import com.amazonaws.services.kinesis.model.HashKeyRange;
import it.enryold.quasarflow.kinesis.streams.producer.hashproviders.interfaces.IExplicitShardKeyHelper;

import java.util.SplittableRandom;
import java.util.stream.Collectors;

public class ExplicitShardKeyHelper implements IExplicitShardKeyHelper {


    private static final String CHARS = "0123456789";
    private static final int LESS_SIGNIFICANT_LENGTH = 20;
    private SplittableRandom random = new SplittableRandom();
    private String shardId;
    private Long upperMostSignificant;
    private Long lowerMostSignificant;


    public ExplicitShardKeyHelper(String shardId, HashKeyRange range)
    {
        this.shardId = shardId;

        int upperSubstring = range.getEndingHashKey().length()-LESS_SIGNIFICANT_LENGTH;
        int lowerSubstring = range.getStartingHashKey().length()-LESS_SIGNIFICANT_LENGTH;

        this.upperMostSignificant = Long.valueOf(range.getEndingHashKey().substring(0, upperSubstring))+1;
        this.lowerMostSignificant = range.getStartingHashKey().equals("0") ? 0L : Long.valueOf(range.getStartingHashKey().substring(0, lowerSubstring))-1;
    }

    public String getShardId() {
        return shardId;
    }

    @Override
    public String generateHashKey() {
        String mostSignificant = String.valueOf(random.nextLong(lowerMostSignificant, upperMostSignificant));

        String randomNumber = random.ints(LESS_SIGNIFICANT_LENGTH, 0, CHARS.length()).mapToObj(i -> "" + CHARS.charAt(i))
                .collect(Collectors.joining());

        return mostSignificant + randomNumber;
    }
}
