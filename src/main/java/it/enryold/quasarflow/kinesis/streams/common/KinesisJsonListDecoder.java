package it.enryold.quasarflow.kinesis.streams.common;

import com.amazonaws.services.kinesis.model.Record;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import it.enryold.quasarflow.interfaces.ITransform;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class KinesisJsonListDecoder<I> implements ITransform<Record, List<I>> {

    private CollectionType typeReference;
    private static ObjectMapper mapper = new ObjectMapper();
    private Class<I> clazz;

    public KinesisJsonListDecoder(Class<I> clazz){
        this.clazz = clazz;
    }

    @Override
    public List<I> apply(Record record) {

        ByteBuffer buff = record.getData();

        if(typeReference == null){
            typeReference =
                    TypeFactory.defaultInstance().constructCollectionType(List.class, clazz);
        }

        try {
            List<I> objs;

            if(buff.hasArray())
            {
                objs = mapper.readValue(buff.array(), typeReference);
            }
            else
            {
                buff.clear();
                objs = mapper.readValue(StandardCharsets.UTF_8.decode(buff).toString(),typeReference);
            }

            return objs;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
