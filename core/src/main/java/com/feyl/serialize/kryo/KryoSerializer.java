package com.feyl.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.feyl.exception.SerializeException;
import com.feyl.remoting.dto.RpcRequest;
import com.feyl.remoting.dto.RpcResponse;
import com.feyl.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Kryo 序列化器
 * Kryo 官方文档：https://github.com/EsotericSoftware/kryo/wiki/Kryo-v4
 * 中文翻译：https://blog.csdn.net/fanjunjaden/article/details/72823866
 *
 * @author Feyl
 */
public class KryoSerializer implements Serializer {

    /**
     * 由于 Kryo 不是线程安全的。每个线程都应该有自己的 Kryo，Input 和 Output 实例。
     * 所以，使用 ThreadLocal 存放 Kryo 对象
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        /**
         * 默认情况下，写入完整类名，然后写入该对象的字节。
         * RpcRequest.class 注册到了 Kryo，它将该类与一个 int 型的 ID 相关联。
         * 当 Kryo 写出 RpcRequest.class 的一个实例时，它会写出这个 int ID。这比写出类名更有效。
         * 在反序列化期间，注册的类必须具有序列化期间相同的 ID 。
         * 注册时可以明确指定特定 ID；若未指定，注册方法分配下一个可用的最小整数 ID。
         */
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Output output = new Output(baos)) {
            Kryo kryo = kryoThreadLocal.get();
            // Object -> byte：将对象序列化为 byte 数组
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (Exception e) {
            throw new SerializeException("Kryo serialization failed");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        Input input = new Input(bais)) {
            Kryo kryo = kryoThreadLocal.get();
            // byte -> Object：从byte数组反序列化出对象
            Object obj = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return clazz.cast(obj);
        } catch (Exception e) {
            throw new SerializeException("Kryo deserialization failed");
        }
    }

}
