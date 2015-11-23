import java.awt.Color;
import java.awt.image.SampleModel;
import java.io.*;
import java.net.*;
import java.util.*;

public class CCClient {

	static String host;
	static int port;
	final static String CRLF="\r\n";
	public static int wstart;
	static long totalTime;
	public static int lastAck = 0;
	static int sent = 1;
	static long[] send_timer;

	static long startTime;
	static long endTime;
	public static int timeOut;
	public static int DevRTT;
	public static int SampleRTT;
	public static final double alpha=0.125;
	public static final double beta=0.25;
    public static Socket socket;
    public static String fileName;
	/**
	 * @param args
	 */
    CCClient(Socket sock, double estimated_delay_ms, String fname){
        timeOut = (int) estimated_delay_ms;
        socket = sock;
        fileName = fname;
    }

	public static void sendFile() {
		try
		{
			socket.setTcpNoDelay(true);

            // first read file and figure out the numebr of packets to send
            File file=new File(fileName);

            // get a buffer of size 1004
            byte[] buffer = new byte[1004];
            FileInputStream fin= new FileInputStream(file);

            int fileSize = (int) file.length();
			int noPackets = fileSize / 1000;
            if (fileSize % 1000 != 0){
                // need to consider the "extra" bytes
                noPackets ++;
            }
			System.out.println("File " + fileName + " has " + noPackets + " packets");

			//reader and writer:
            DataOutputStream writer = new DataOutputStream(socket.getOutputStream());

			//define the thread and start it
            Thread thread = new Thread(new Listener(socket, noPackets));
            thread.start();

			//send the noPackets to the server
            writer.write(noPackets);

			lastAck=0;
			sent=1;
			int cwnd = 1;
			int ssthresh = 16;
            int second_last_ack = 0;
			int RTT_count = 0;
            boolean timeOutOccured = false;
            int nRTT = 0;
            int send_segment = 0;
            long begin = System.currentTimeMillis();

			startTime=System.currentTimeMillis();
			try {
				while(sent<=noPackets)
				{
                    System.out.println("Current cwnd: " + cwnd);
                    while (sent - lastAck <= cwnd && sent <= noPackets){
                        send_segment = 1;
                        System.out.println(nRTT + " !!Client sending packet: " + sent);
                        writer.write(sent);
                        sent +=1;
                    }
                    nRTT += send_segment;
                    send_segment = 0;
                    startTime = System.currentTimeMillis();
                    timeOutOccured = false;
                    // Keep waiting until either: 1) timeout occurs; or 2) lastAck==send
                    while (lastAck < sent - 1){
                        // System.out.println("lastAck: " + lastAck+"; sent: "+sent);
                        Thread.sleep(1);
                        // Timeout occurs!!!
                        if ((System.currentTimeMillis() - startTime) > timeOut) {
                            System.out.println("Timeout!");
                            // reset sent
                            sent = lastAck+1;
                            // reset ssthresh and cwnd
                            ssthresh = cwnd/2;
                            cwnd = 1;
                            timeOutOccured = true;
                            break;
                        }
                    }
                    if (!timeOutOccured) {
                        // adjust cwnd size (slow start)
                        if (cwnd*2 <= ssthresh)
                            cwnd *= 2;
                        else
                            cwnd++;
                    }
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				endTime = System.currentTimeMillis();
				totalTime = endTime - startTime;
			}


			//print the total taken time, number of sucessfully sent packets, etc.
			//...

			// writer.flush();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void update(int ackNum)
	{
		//update lastAck here. note that last ack is accumulative,
		//i.e., if ack for packet 10 is previously received and now ack for packet 7 is received, lastAck will remain 10
        CCClient.lastAck = ackNum;
	}

}
