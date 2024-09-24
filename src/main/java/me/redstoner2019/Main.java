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
        ServerSocket serverSocket = new ServerSocket(100);
        System.out.println("Listening on port 100...");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(101);
                    while (true) {
                        Socket socket = serverSocket.accept();
                        System.out.println("Accepted connection from " + socket.getRemoteSocketAddress());
                        socket.getOutputStream().write("Hello World!\n".getBytes());
                        socket.getOutputStream().flush();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        t.start();

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Client connected");
            ClientHandler client = new ClientHandler(socket);
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