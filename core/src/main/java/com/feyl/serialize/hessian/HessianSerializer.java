package com.feyl.serialize.hessian;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.feyl.exception.SerializeException;
import com.feyl.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Hessian is a dynamically-typed, binary serialization and
 *  Web Services protocol designed for object-oriented transmission.
 *
 * @author Feyl
 */
public class HessianSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            HessianOutput output = new HessianOutput(baos);
            output.writeObject(obj);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new SerializeException("Hessian serialization failed");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            HessianInput input = new HessianInput(bais);
            Object o = input.readObject();
            return clazz.cast(o);
        } catch (IOException e) {
            throw new SerializeException("Hessian deserialization failed");
        }
    }

}
