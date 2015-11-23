import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.PriorityQueue;
import java.util.Collections;
import java.util.Random;
import java.util.Arrays;

import javax.xml.ws.handler.MessageContext.Scope;

// The network is represented by a graph, that contains nodes and edges
class Node implements Comparable<Node>
{
	public final int name;
	public Edge[] neighbors;
	public double minDistance = Double.POSITIVE_INFINITY;
	public Node previous;     // to keep the path
	public Node(int argName) 
	{ 
		name = argName; 
	}

	public int compareTo(Node other)
	{
		return Double.compare(minDistance, other.minDistance);
	}
}

class Edge
{
	public final Node target;
	public final double weight;
	public Edge(Node argTarget, double argWeight)
	{ 
		target = argTarget;
		weight = argWeight; 
	}
}

public class RoutingClient {

	static String mode;
	static String host;
	static int port;

	public static void adjacenyToEdges(double[][] matrix, List<Node> v)
	{
        for(int i = 0; i < matrix.length; i++) {
			v.get(i).neighbors = new Edge[matrix.length];
			for(int j = 0; j < matrix.length; j++)
			{
				v.get(i).neighbors[j] =  new Edge(v.get(j), matrix[i][j]);	
			}
		}
	}
	public static void computePaths(Node source)
	{
		// Complete the body of this function
        PriorityQueue<Node> unvisited = new PriorityQueue<Node>();

        // first add the source node
        unvisited.add(source);
        source.minDistance = 0;

        while(unvisited.size() != 0){
            Node src = unvisited.poll();
            for (Edge edge : src.neighbors){
                Node target = edge.target;
                double distThroughSrc = src.minDistance + edge.weight;
                if(Double.isInfinite(target.minDistance) || distThroughSrc < target.minDistance){
                    unvisited.remove(target);
                    target.minDistance = distThroughSrc;
                    target.previous = src;
                    unvisited.add(target);
                }
            }
        }
	}

	public static List<Integer> getShortestPathTo(Node target)
	{
        List<Integer> path = new ArrayList<Integer>();
        while(target != null){
            path.add(target.name);
            target = target.previous;
        }
        Collections.reverse(path);
        return path;
	}

	/**
	 * @param args
	 */

	public static void main(String[] args) {

		if(args.length<=0)
		{
			mode="client";
			host="localhost";
			port=9876;
		}
		else if(args.length==1)
		{
			mode=args[0];
			host="localhost";
			port=9876;
		}
		else if(args.length==3)
		{
			mode=args[0];
			host=args[1];
			port=Integer.parseInt(args[2]);
		}
		else
		{
			System.out.println("improper number of arguments.");
			return;
		}

		try 
		{
			Socket socket=null;
			if(mode.equalsIgnoreCase("client"))
			{
				socket=new Socket(host, port);
			}
			else if(mode.equalsIgnoreCase("server"))
			{
				ServerSocket ss=new ServerSocket(port);
				socket=ss.accept();
			}
			else
			{
				System.out.println("improper type.");
				return;
			}
			System.out.println("Connected to : "+ host+ ":"+socket.getPort());

			//reader and writer:
			BufferedReader reader=new BufferedReader(new InputStreamReader(socket.getInputStream())); //for reading lines
			DataOutputStream writer=new DataOutputStream(socket.getOutputStream());	//for writing lines.
			Scanner scr = new Scanner(System.in);
			
			while(socket!=null && socket.isConnected() && !socket.isClosed()){

				// Send noNodes to the server, and read a String from it containing adjacency matrix
                String noNodes_str = reader.readLine();
                int noNodes = Integer.parseInt(noNodes_str);
                String adjacency_mat = reader.readLine();
				
				// Create an adjacency matrix after reading from server
				double[][] matrix = new double[noNodes][noNodes];
				
				// Use StringToenizer to store the values read from the server in matrix
                StringTokenizer st = new StringTokenizer(adjacency_mat);
                int i, j;
                for(i = 0; i < noNodes; i++){
                    for(j = 0; j < noNodes; j++){
                        String tok = st.nextToken();
                        matrix[i][j] = Double.parseDouble(tok);
                    }
                }
                if(st.hasMoreTokens()){
                    System.out.println("ERROR: Not all tokens are completely parsed!");
                    throw new NullPointerException();
                }

				//The nodes are stored in a list, nodeList
				List<Node> nodeList = new ArrayList<Node>();
				for(i = 0; i < noNodes; i++){
					nodeList.add(new Node(i));
				}
				
				// Create edges from adjacency matrix
				adjacenyToEdges(matrix, nodeList);
				
				// Finding shortest path for all nodes
                // FROM 0 to noNodes - 1
                i = 0;
                j = noNodes - 1;

                computePaths(nodeList.get(i));
                System.out.println("Node " + i + ":");

                // get the shortest path
                List<Integer> path = getShortestPathTo(nodeList.get(j));

                // compute total time
                int path_size = path.size();
                int k;
                double total_time = 0;
                for(k = 0; k < path_size - 1; k++){
                    total_time += matrix[path.get(k)][path.get(k+1)];
                }
                System.out.println("Selected route to node " + j + " is " + total_time + "ms, path " + path);
                writer.writeBytes(path.toString() + "\r\n");

                // Initiate file transfer in another class

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
