package controllers;

import org.apache.log4j.Logger;
import utils.GetPropertieValue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Connections {
    GetPropertieValue getPropertieValue = new GetPropertieValue();

    Logger LOGGER = Logger.getLogger(Connections.class);
    private Socket socket;

    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    private final String ADDRESS = getPropertieValue.getADDRESS();
    private final int PORT = getPropertieValue.getPORT();

    private final byte BYTE_COUNT = 5;
    private static final long TIMEOUT = 500;


    public Boolean server_connection1() {
        Boolean no_errors = true;
        if (socket == null || socket.isClosed()) {
            try {
                socket = new Socket(ADDRESS, PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                dataInputStream = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (socket == null && dataOutputStream == null && dataInputStream == null) {
                no_errors = false;
            }
        }
        return no_errors;
    }

    public String getSendToServer(String sendToServer) {
        server_connection();

        try {
            dataOutputStream.write(sendToServer.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] buffer = new byte[1024];
        int cnt = 0;

        try {
            cnt = dataInputStream.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String receiveFromServer = new String(buffer, 0, cnt);
        LOGGER.info("received from server: " + receiveFromServer);

        return receiveFromServer;
    }

    public <C extends SelectableChannel & ReadableByteChannel> String receive(C chan) {
        ByteBuffer inputData = ByteBuffer.allocate(1024);
        int cnt = 0;
        try {
            Selector sel = Selector.open();
            SelectionKey key = chan.register(sel, SelectionKey.OP_READ);
            long timeout = TIMEOUT;
            while (inputData.hasRemaining()) {
                if (timeout < 0L) {
                    throw new IOException(String.format("Timed out, %d of %d bytes read", inputData.position(), inputData.limit()));
                }
                long startTime = System.nanoTime();
                sel.select(timeout);
                long endTime = System.nanoTime();
                timeout -= TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                if (sel.selectedKeys().contains(key)) {
                    chan.read(inputData);
                }
                sel.selectedKeys().clear();
            }
        } catch (Exception e) {
            try {
                throw new Exception("Couldn't receive data from modem: " + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        String receiveFromServer = new String(inputData.array(), 0, cnt);
        LOGGER.info("received from server: " + receiveFromServer);
        return receiveFromServer;
    }


    public Boolean server_connection() {
        Boolean no_errors = true;

        try {
            Selector selector = Selector.open();

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

            InetSocketAddress inetSocketAddress = new InetSocketAddress(ADDRESS, PORT);

            serverSocketChannel.bind(inetSocketAddress);
            serverSocketChannel.configureBlocking(false);

            int ops = serverSocketChannel.validOps();

            SelectionKey selectKy = serverSocketChannel.register(selector, ops, null);

            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> selectionKeyIterator = selectionKeys.iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey myKey = selectionKeyIterator.next();
                    if (myKey.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_WRITE);
                        LOGGER.info("Connection Accepted: " + socketChannel.getLocalAddress() + "\n");
                    } else if (myKey.isWritable()) {
                        byte[] message = new String("blablabla").getBytes();
                        ByteBuffer buffer = ByteBuffer.wrap(message);
                        SocketChannel client1 = (SocketChannel) myKey.channel();
                        ByteBuffer buffer1 = ByteBuffer.allocate(256);
                        client1.write(buffer);
                    } else if (myKey.isReadable()) {
                        SocketChannel client = (SocketChannel) myKey.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        client.read(buffer);

                        String result = new String(buffer.array()).trim();
                        LOGGER.info("receive:" + result);

                    }
                    selectionKeyIterator.remove();
                }
            }


        } catch (
                IOException e) {
            e.printStackTrace();
        }


        return no_errors;
    }


}
