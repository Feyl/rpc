package com.feyl.remoting.transport.socket;

import com.feyl.factory.SingletonFactory;
import com.feyl.remoting.dto.RpcRequest;
import com.feyl.remoting.dto.RpcResponse;
import com.feyl.remoting.handler.RpcRequestHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author Feyl
 */
@Slf4j
public class SocketRpcRequestHandler implements Runnable{
    private final Socket socket;

    private final RpcRequestHandler rpcRequestHandler;


    public SocketRpcRequestHandler(Socket socket) {
        this.socket = socket;
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void run() {
        log.info("socket server handle message from client by thread: [{}]", Thread.currentThread().getName());
        try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
            RpcRequest rpcRequest = (RpcRequest) ois.readObject();
            Object result = rpcRequestHandler.handle(rpcRequest);
            oos.writeObject(RpcResponse.success(rpcRequest.getRequestId(), result));
            oos.flush();
        } catch (IOException | ClassNotFoundException e) {
            log.error("occur exception when socket server handling message from client");
        }
    }
}
