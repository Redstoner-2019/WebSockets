package me.redstoner2019;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class Main {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(99);
        System.out.println("Listening on port 99...");
        Socket client = serverSocket.accept();
        System.out.println("Client connected");

        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        OutputStream out = client.getOutputStream();

        String data;
        String secWebSocketKey = null;
        while (!(data = in.readLine()).isEmpty()) {
            System.out.println("Client request: " + data);
            if (data.startsWith("Sec-WebSocket-Key")) {
                secWebSocketKey = data.split(": ")[1];
            }
        }

        String acceptKey = generateSecWebSocketAccept(secWebSocketKey);

        // Respond with the handshake acceptance
        String response = "HTTP/1.1 101 Switching Protocols\r\n"
                + "Upgrade: websocket\r\n"
                + "Connection: Upgrade\r\n"
                + "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";
        out.write(response.getBytes());
        out.flush();

        while (true) {
            String line = readWebSocketFrame(client.getInputStream());
            System.out.println(line);
        }
    }

    private static String generateSecWebSocketAccept(String secWebSocketKey) throws Exception {
        String webSocketKeyMagic = secWebSocketKey.trim() + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hash = sha1.digest(webSocketKeyMagic.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(hash);
    }

    private static String readWebSocketFrame(InputStream in) throws IOException {
        int b1 = in.read();
        boolean fin = (b1 & 0x80) != 0;
        int opcode = b1 & 0x0F;

        int b2 = in.read();
        boolean masked = (b2 & 0x80) != 0;
        int payloadLength = b2 & 0x7F;

        if (payloadLength == 126) {
            payloadLength = (in.read() << 8) | in.read();
        } else if (payloadLength == 127) {
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.put(in.readNBytes(8));
            payloadLength = (int) buffer.getLong();
        }

        byte[] maskingKey = null;
        if (masked) {
            maskingKey = new byte[4];
            in.read(maskingKey);
        }

        byte[] payloadData = new byte[payloadLength];
        in.read(payloadData);

        if (masked) {
            for (int i = 0; i < payloadLength; i++) {
                payloadData[i] ^= maskingKey[i % 4];
            }
        }

        return new String(payloadData);
    }
    private static void sendWebSocketFrame(OutputStream out, String message) throws IOException {
        byte[] payloadData = message.getBytes();
        int frameHeader = 0x81;

        out.write(frameHeader);

        if (payloadData.length <= 125) {
            out.write(payloadData.length);
        } else if (payloadData.length <= 65535) {
            out.write(126);
            out.write((payloadData.length >> 8) & 0xFF);
            out.write(payloadData.length & 0xFF);
        } else {
            out.write(127);
            for (int i = 7; i >= 0; i--) {
                out.write((payloadData.length >> (i * 8)) & 0xFF);
            }
        }

        out.write(payloadData);
        out.flush();
    }
}