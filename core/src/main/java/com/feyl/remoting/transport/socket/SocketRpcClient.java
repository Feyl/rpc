package com.feyl.remoting.transport.socket;

import com.feyl.exception.RpcException;
import com.feyl.extension.ExtensionLoader;
import com.feyl.registry.ServiceDiscovery;
import com.feyl.remoting.dto.RpcRequest;
import com.feyl.remoting.transport.RpcRequestTransport;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 基于 Socket 传输 RPC请求 客户端
 *
 * @author Feyl
 */
@AllArgsConstructor
@Slf4j
public class SocketRpcClient implements RpcRequestTransport {

    private final ServiceDiscovery serviceDiscovery;

    public SocketRpcClient() {
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zookeeper");
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            // 通过输出流向服务端发送数据
            oos.writeObject(rpcRequest);
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            // 通过输入流读取远程调用响应结果
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RpcException("Socket客户端调用服务失败", e);
        }
    }
}
