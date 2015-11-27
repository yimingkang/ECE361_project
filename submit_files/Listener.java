/*
Group code: y0z3
Zexuan Wang 998851773
Yiming Kang 998676730
*/
import java.net.*;
import java.io.*;

public class Listener implements Runnable {
    private Socket socket;
    private int nPack;
    private int[] ackArray;

    public Listener (Socket s, int n){
        System.out.println("Listener object is created!");
        socket = s; 
        nPack = n;
        ackArray = new int[n + 1];
    }

    @Override 
    public void run() {
        try{ 
            BufferedReader socket_reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // DataInputStream readInputStream = new DataInputStream(socket.getInputStream());

            // byte[] buffer = new byte[8];
            while(true){
                // readInputStream.read(buffer);
                // int ackNum = (int)(buffer[0]);
                int ackNum = Integer.parseInt(socket_reader.readLine());
                System.out.println("Client got ack: " + ackNum);
                ackArray[ackNum] = 1;
                if(ackNum != CCClient.lastAck + 1){
                    continue;
                }
                while (ackNum <= nPack){
                    // find the next unfilled hole
                    if(ackArray[ackNum] != 1){
                        ackNum--;
                        break;
                    }
                    if (ackNum != nPack){
                        ackNum++;
                    } else {
                        break;
                    }
                }
                // System.out.println("BufferedReader gets:" + input);
                // int ackNum = Integer.parseInt(input);
                System.out.println("Client updating ack num to: " + ackNum);
                CCClient.update(ackNum);
                if (ackNum == nPack)
                    break;
            }
        }
        catch (Exception e) {e.getStackTrace();}
        
    }
}
