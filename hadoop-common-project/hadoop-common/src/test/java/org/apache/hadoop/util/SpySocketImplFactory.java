package org.apache.hadoop.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.SocketImplFactory;

public class SpySocketImplFactory implements SocketImplFactory {

    public interface SocketSpy {
        void onSocketCreated(SocketImpl socket);
    }
    
    private final SocketSpy spy;

    public SpySocketImplFactory(SocketSpy spy) {
        this.spy = spy;
    }

    @Override
    public SocketImpl createSocketImpl() {
        SocketImpl socket = newSocketImpl();
        spy.onSocketCreated(socket);
        return socket;
    }

    public Socket getSocket(SocketImpl impl) {
        try {
            Method getSocket = SocketImpl.class.getDeclaredMethod("getSocket");
            getSocket.setAccessible(true);
            return (Socket) getSocket.invoke(impl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ServerSocket getServerSocket(SocketImpl impl) {
        try {
            Method getServerSocket = SocketImpl.class.getDeclaredMethod("getServerSocket");
            getServerSocket.setAccessible(true);
            return (ServerSocket) getServerSocket.invoke(impl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private SocketImpl newSocketImpl() {
        try {
            Class<?> defaultSocketImpl = Class.forName("java.net.SocksSocketImpl");
            Constructor<?> constructor = defaultSocketImpl.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (SocketImpl) constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
