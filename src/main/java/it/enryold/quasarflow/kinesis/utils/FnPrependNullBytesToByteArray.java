package it.enryold.quasarflow.kinesis.utils;

import co.paralleluniverse.fibers.Suspendable;

import java.util.function.Function;

public class FnPrependNullBytesToByteArray implements Function<byte[], Function<Integer, byte[]>> {

    @Override
    @Suspendable
    public Function<Integer, byte[]> apply(byte[] bytes) {


        return expectedSize -> {

            int delta = expectedSize-bytes.length;

            byte[] nullByteArray = new byte[delta];

            for(int i=0; i<delta; i++)
                nullByteArray[i] = (byte)0;

            byte[] destination = new byte[expectedSize];
            System.arraycopy(nullByteArray, 0, destination, 0, nullByteArray.length);
            System.arraycopy(bytes, 0, destination, nullByteArray.length, bytes.length);

            return destination;
        };
    }
}
