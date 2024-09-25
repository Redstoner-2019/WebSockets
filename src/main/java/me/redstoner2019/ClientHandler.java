package me.redstoner2019;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class ClientHandler {
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;

    public ClientHandler(Socket client) {
        try {
            this.socket = client;
            this.in = client.getInputStream();
            this.out = client.getOutputStream();

            socket.setTcpNoDelay(true);

            BufferedReader inp = new BufferedReader(new InputStreamReader(in));

            String data;
            String secWebSocketKey = null;
            while (!(data = inp.readLine()).isEmpty()) {
                System.out.println("Client request: " + data);
                if (data.startsWith("Sec-WebSocket-Key")) {
                    secWebSocketKey = data.split(": ")[1];
                }
            }

            String acceptKey = generateSecWebSocketAccept(secWebSocketKey);

            String response = "HTTP/1.1 101 Switching Protocols" + System.lineSeparator()
                    + "Upgrade: websocket" + System.lineSeparator()
                    + "Access-Control-Allow-Origin: *" + System.lineSeparator()
                    + "Access-Control-Allow-Methods: GET, POST, OPTIONS" + System.lineSeparator()
                    + "Access-Control-Allow-Headers: Content-Type" + System.lineSeparator()
                    + "Connection: Upgrade" + System.lineSeparator()
                    + "Sec-WebSocket-Accept: " + acceptKey + "\n";

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

            for (int i = 0; i < bytes.length; i+=10) {
                for (int j = 0; j < 10; j++) {
                    int k = i + j;
                    if(k < bytes.length){
                        System.out.printf((j == 0 ? "" : "| ") + "%3d ", bytes[k]);
                    }
                }
                System.out.println();
            }

            out.write(bytes);
            out.flush();

            System.out.println("Writing and flushing done.");
        } catch (Exception e) {
            System.out.println("An error occured");
            e.printStackTrace();
        }

        System.out.println("Setup done");
        while (true) {
            /*System.out.println("Waiting for client request...");
            try {
                String msg = Arrays.toString(in.readAllBytes());
                System.out.println("Received: " + msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }*/
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
