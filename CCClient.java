import java.awt.Color;
import java.awt.image.SampleModel;
import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.channels.FileChannel;

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

            // a file channel is used to set file pointer position
            FileChannel fc = fin.getChannel();

            int fileSize = (int) file.length();
			int noPackets = fileSize / 1000;
            if (fileSize % 1000 != 0){
                // need to consider the "extra" bytes
                noPackets ++;
            }
			System.out.println("File " + fileName + " has " + noPackets + " packets");
            System.out.println("TimeOut value is set to: " + timeOut + " ms");

			//reader and writer:
            DataOutputStream writer = new DataOutputStream(socket.getOutputStream());

            // send file name and noPackets to server
            writer.writeBytes(fileName + CRLF);
            writer.writeBytes(Integer.toString(noPackets) + CRLF);

			//define the thread and start it
            Thread thread = new Thread(new Listener(socket, noPackets));
            thread.start();

			lastAck=0;
			sent=1;
			int cwnd = 1;
			int ssthresh = 16;
            boolean timeOutOccured = false;

			startTime=System.currentTimeMillis();
			try {
				while(sent<=noPackets)
				{
                    System.out.println("Current cwnd: " + cwnd);
                    while (sent - lastAck <= cwnd && sent <= noPackets){
                        System.out.println(" Client sending packet: " + sent);

                        // read from position 4, up to 1000 chars
                        int len = fin.read(buffer, 4, 1000);
                        if (len<0){
                            // this is BAD
                            System.out.println("ERROR: EOF reached, impossiburrrr");
                            throw new Exception();
                        }else{
                            // first fill in the 'packet' field ...
                            // little endian
                            buffer[3] = (byte) (sent >> 0);
                            buffer[2] = (byte) (sent >> 8);
                            buffer[1] = (byte) (sent >> 16);
                            buffer[0] = (byte) (sent >> 24);
                            // write everyting out
                            writer.write(buffer, 0, len + 4);
                            sent +=1;
                        }
                    }
                    startTime = System.currentTimeMillis();
                    timeOutOccured = false;
                    // Keep waiting until either: 1) timeout occurs; or 2) lastAck==send
                    while (lastAck < sent - 1){
                        Thread.sleep(1);
                        if ((System.currentTimeMillis() - startTime) > timeOut) {
                            System.out.println("Timeout!");

                            // reset sent
                            sent = lastAck+1;

                            // reset file pointer
                            fc.position((sent-1) * 1000);

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
