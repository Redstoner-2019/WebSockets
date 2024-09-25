package me.redstoner2019;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Tester {
    public static void main(String[] args) throws IOException {
        //Socket socket = new Socket("158.220.105.209",100);
        Socket socket = new Socket("localhost",100);

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

        System.out.println("Recieving Started");

        while (true){
            try{
                byte[] bytes = new byte[4096];

                int recieved = is.read(bytes);

                byte[] b = new byte[recieved];

                System.arraycopy(bytes, 0, b, 0, b.length);

                System.out.println("Recieved " + recieved);

                for (int i = 0; i < recieved; i+=10) {
                    for (int j = 0; j < 10; j++) {
                        int k = i + j;
                        if(k < recieved){
                            System.out.printf((j == 0 ? "" : "| ") + "%3d ", b[k]);
                        }
                    }
                    System.out.println();
                }

                System.out.println();

                Scanner sc = new Scanner(new String(b));
                while(sc.hasNext()){
                    String line = sc.nextLine();
                    System.out.println(line);
                    if(line.isEmpty()){
                        System.out.println("Connection complete");
                        break;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                break;
            }
        }
    }
}
