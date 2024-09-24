package me.redstoner2019;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class Tester {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("158.220.105.209",100);
        //Socket socket = new Socket("localhost",100);

        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();

        String s = "GET / HTTP/1.1\n" +
                "Host: localhost:99\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/115.0\n" +
                "Accept: */*\n" +
                "Accept-Language: en-US,en;q=0.5\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "Sec-WebSocket-Version: 13\n" +
                "Origin: http://localhost:63342\n" +
                "Sec-WebSocket-Extensions: permessage-deflate\n" +
                "Sec-WebSocket-Key: gZuGLOQETclVZkr5VcMEyg==\n" +
                "Connection: keep-alive, Upgrade\n" +
                "Cookie: Idea-a788c437=c4517cb0-ef4e-4a4b-9ed2-99eb17930633\n" +
                "Sec-Fetch-Dest: websocket\n" +
                "Sec-Fetch-Mode: websocket\n" +
                "Sec-Fetch-Site: same-site\n" +
                "Pragma: no-cache\n" +
                "Cache-Control: no-cache\n" +
                "Upgrade: websocket\n\n";


        os.write(s.getBytes());
        os.flush();

        BufferedReader bf = new BufferedReader(new InputStreamReader(is));

        while (true){
            /*byte[] read = is.readAllBytes();
            System.out.println("Recieving");
            System.out.println(Arrays.toString(read));
            System.out.println(new String(read));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }*/
            System.out.println("Recieving");
            new String();
            String line = bf.readLine();
            System.out.println(line);
            System.out.println(line.isEmpty());
        }
    }
}
