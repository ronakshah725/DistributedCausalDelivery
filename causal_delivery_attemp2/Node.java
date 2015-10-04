package causal_delivery_attemp2;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;


public class Node {
	
	int id;
	String host;
	int noOfNodes = 10;
	int port;
	int basePort = 9000;
	String controllerHostName = "dc11.utdallas.edu";
	
	HashMap<Integer,NodeDef> store = new HashMap<Integer,NodeDef>();
	
	Node(String id) {
		
		this.id = Integer.parseInt(id);
		try {
			this.host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.port = this.basePort + this.id;

	}

	public String toString() {

		return id + "@" + host + ":" + port;
	}
	public static void main(String[] args) {
		
		//todo update store
		Node me = new Node("01");
		System.out.println("Node " + me + " is running.");
		
		
		//temporary local node update
		
		for (int i = 1; i < 12; i++) {
			
			me.store.put(i, new NodeDef(i, me.host, me.basePort + i));
		}
		new writingSocketThread(me, 11, "up");
		
		
		

	}

}


class writingSocketThread extends Thread {

	Node n;
	String msg;
	int dstId;

	public writingSocketThread(Node n, int dstId, String msg) {

		this.n= n;
		this.dstId =dstId;
		this.msg = msg;
	}


	public void run() {
		try{
			
			int port = n.store.get(dstId).port;
			String host = n.store.get(dstId).host;
			InetAddress address = InetAddress.getByName(host);
			Socket dstSocket = new Socket(address, port);
			PrintWriter out=new PrintWriter(dstSocket.getOutputStream(), true);
			out.println(msg);
			dstSocket.close();	
			
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
