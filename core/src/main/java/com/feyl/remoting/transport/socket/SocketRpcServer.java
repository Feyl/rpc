package com.feyl.remoting.transport.socket;

import java.util.concurrent.ExecutorService;

/**
 * @author Feyl
 */
public class SocketRpcServer {

    private final ExecutorService threadPool;


    public SocketRpcServer() {
        this.threadPool = null;
    }
}
