package controllers;

import org.apache.log4j.Logger;
import utils.GetPropertieValue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Connections {
    GetPropertieValue getPropertieValue = new GetPropertieValue();

    Logger LOGGER = Logger.getLogger(Connections.class);
    private Socket socket;

    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    private final String ADDRESS = getPropertieValue.getADDRESS();
    private final int PORT = getPropertieValue.getPORT();

    private final byte BYTE_COUNT = 5;

    public Boolean server_connection() {
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

        byte[] buffer = new byte[256];
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


}
