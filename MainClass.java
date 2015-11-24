import java.net.*;
import java.util.*;
import java.io.*;

public class MainClass {
    public static Socket socket;
    public static double estimatedTimeout;

    public static void main(String[] args){
        try{
            // find delay
            RoutingClient router = new RoutingClient();
            socket = router.getSock();
            estimatedTimeout = router.getEstimatedRTT();
            System.out.println("Esimated RTT is: " + estimatedTimeout);

            // get file name from client
            System.out.println("Enter file name:");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String fileName = reader.readLine();
            System.out.println("File name is: " + fileName);


            // send it off to a file client
            CCClient sender = new CCClient(socket, estimatedTimeout, fileName);
            sender.sendFile();
            while(!socket.isClosed()){
                Thread.sleep(1000);
            }
        } catch (Exception e) {e.getStackTrace();}
        System.out.println("Exiting...");
    }
}
