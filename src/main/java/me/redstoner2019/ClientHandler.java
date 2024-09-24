package me.redstoner2019;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

public class ClientHandler extends Thread {
    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public ClientHandler(Socket client) {
        this.socket = client;
        try {
            this.in = client.getInputStream();
            this.out = client.getOutputStream();

            BufferedReader inp = new BufferedReader(new InputStreamReader(client.getInputStream()));
            OutputStream out = client.getOutputStream();

            String data;
            String secWebSocketKey = null;
            while (!(data = inp.readLine()).isEmpty()) {
                System.out.println("Client request: " + data);
                if (data.startsWith("Sec-WebSocket-Key")) {
                    secWebSocketKey = data.split(": ")[1];
                }
            }

            String acceptKey = generateSecWebSocketAccept(secWebSocketKey);
            acceptKey = "kjhkldfghlkjh";

            //String response = "HTTP/1.1 101 Switching Protocols\r\n"
            //        + "Upgrade: websocket\r\n"
            //        + "Access-Control-Allow-Origin: *\r\n"
            //        + "Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n"
            //        + "Access-Control-Allow-Headers: Content-Type\r\n"
            //        + "Connection: Upgrade\r\n"
            //        + "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";

            System.out.println();

            String response = "HTTP/1.1 101 Switching Protocol\n"
                    + "Upgrade: websocket\n"
                    + "Connection: Upgrade\n"
                    + "Sec-WebSocket-Accept: " + acceptKey + "\n\n";

            //response = "xjkdfhgxcj,.hjgjklxdngkljxdhnjgjkxdflhgkljxdfghnxkldjfghnxdfkljfghxdljkfghnxkdjlfghxdljkfghxlidrjhzislrjhtlxdjköfghxdkjfhgxdoölfjhgsdjköflktzlörh\nxjkdfhgxcj,.hjgjklxdngkljxdhnjgjkxdflhgkljxdfghnxkldjfghnxdfkljfghxdljkfghnxkdjlfghxdljkfghxlidrjhzislrjhtlxdjköfghxdkjfhgxdoölfjhgsdjköflktzlörh\nxjkdfhgxcj,.hjgjklxdngkljxdhnjgjkxdflhgkljxdfghnxkldjfghnxdfkljfghxdljkfghnxkdjlfghxdljkfghxlidrjhzislrjhtlxdjköfghxdkjfhgxdoölfjhgsdjköflktzlörh\nxjkdfhgxcj,.hjgjklxdngkljxdhnjgjkxdflhgkljxdfghnxkldjfghnxdfkljfghxdljkfghnxkdjlfghxdljkfghxlidrjhzislrjhtlxdjköfghxdkjfhgxdoölfjhgsdjköflktzlörh\nxjkdfhgxcj,.hjgjklxdngkljxdhnjgjkxdflhgkljxdfghnxkldjfghnxdfkljfghxdljkfghnxkdjlfghxdljkfghxlidrjhzislrjhtlxdjköfghxdkjfhgxdoölfjhgsdjköflktzlörh\nxjkdfhgxcj,.hjgjklxdngkljxdhnjgjkxdflhgkljxdfghnxkldjfghnxdfkljfghxdljkfghnxkdjlfghxdljkfghxlidrjhzislrjhtlxdjköfghxdkjfhgxdoölfjhgsdjköflktzlörh\n";

            //String resp = String.format("HTTP/1.1 101 Switching Protocols\nUpgrade: websocket\r\nAccess-Control-Allow-Origin: *\r\nAccess-Control-Allow-Methods: GET, POST, OPTIONS\r\nAccess-Control-Allow-Headers: Content-Type\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: %s\r\n\r\n", acceptKey);

            String chars = "abcdefghijklmnopqrstuvwxyz";

            /*response = "";

            for (int i = 0; i < 129; i++) {
                Random r = new Random();
                response += chars.charAt(r.nextInt(chars.length()));
            }

            response+="\n\n";*/

            System.out.println(response);

            byte[] d = response.getBytes("UTF-8");

            System.out.println();
            System.out.println(new String(d));
            System.out.println(response.length());

            out.write(d);
            out.flush();
            out.close();

            System.out.println("Writing and flushing done.");
        } catch (Exception e) {
            System.out.println("An error occured");
            e.printStackTrace();
        }

        System.out.println("Setup done");
        while (true) {
            System.out.println("Waiting for client request...");
            String msg = null;
            try {
                msg = Arrays.toString(in.readAllBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Received: " + msg);
        }
        //start();
    }

    @Override
    public void run() {
        //sendWebSocketFrame("Welcome to the server!");
        while (true) {
            try {
                System.out.println("Waiting for client request...");
                String msg = readWebSocketFrame();
                System.out.println("Received: " + msg);
                sendWebSocketFrame(msg);
            } catch (IOException e) {
                disconnect();
                System.out.println("Client disconnected");
                e.printStackTrace();
                break;
            }
        }
    }

    private void disconnect() {
        try {
            this.socket.close();
            this.in.close();
            this.out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateSecWebSocketAccept(String secWebSocketKey) throws Exception {
        String webSocketKeyMagic = secWebSocketKey.trim() + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hash = sha1.digest(webSocketKeyMagic.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(hash);
    }

    private String readWebSocketFrame() throws IOException {
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
    private void sendWebSocketFrame(String message){
        try{
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
        }catch (Exception e){

        }

    }
}
