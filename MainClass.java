import java.net.*;
import java.util.*;

public class MainClass {
    public static Socket socket;
    public static double estimatedRTT;

    public static void main(String[] args){
        // find delay
        RoutingClient router = new RoutingClient();
        socket = router.getSock();
        estimatedRTT = router.getEstimatedRTT();
        System.out.println("Esimated RTT is: " + estimatedRTT);

        // get file name from client
        System.out.println("Enter file name:");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String file_name = reader.readLine();
        System.out.println("File name is: " + file_name);


        // send it off to a file client

    }
}
