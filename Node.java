

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;



public class Node {

	int id;
	String host;
	final int noOfNodes = 2;
	int port;
	int basePort = 9000;
	String controllerHostName = "dc11.utdallas.edu";
	boolean established = false;
	ConcurrentHashMap<Integer, PrintWriter> outMap = new ConcurrentHashMap<>();
	
	
	boolean startConnectingToOtherNodes = false;
	boolean highEst=false;


	HashMap<Integer, NodeDef> store = new HashMap<Integer, NodeDef>();


	public static void main(String[] args) throws IOException, InterruptedException {

		
	Node me = new Node(args[0]);
		System.out.println("Node " + me + " is running.");
		me.initStore();
		
		//tell controller that i am up
		new writingSocketThread(me, 11, "up" + "#" + getIDString(me.id)).start();;

		new ListenHandler(me).start();
		
		Thread.sleep(2000);	//wait for all to be up
		
		while(!me.getEst()){}
		System.out.println("est : " + me.getEst());
		//comes till here
		//lets do a broadcast


		
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
//			if (me.startConnectingToOtherNodes) {  
//
//				for (int id = me.NodeID-1; id > 0; id--) {
//					me.new RequestHandler(new Socket(me.getNodeAddress(id), me.BasePort+id));
//					
//
//				}
//			}
			
			System.out.println("Done connections to all");
			System.out.println(me.outMap);

		
		
		
		
		
		
		
		
		for (int i = 1; i<=me.noOfNodes; i++){
			if(i==me.id)continue;
			System.out.println("in broadcast");
			new writingSocketThread(me, i, "hi from " + i);
		}
		
	}
	public void initStore() {
		
		for (int i = 1; i <=11 ; i++) {
			store.put(i, new NodeDef(i, host, basePort + i));
		}
	}
	
	public synchronized void setEst(boolean est){
		established = est;
	}
	
	public synchronized boolean getEst(){
		return established; 
	}

	Node(String id) {
		
		this.id = Integer.parseInt(id);
		try {
			this.host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
		}
		this.port = this.basePort + this.id;

	}

	public String toString() {

		return id + "@" + host + ":" + port;
	}

	static String getIDString(int id) {
		
		if (id < 10) {

			return "0" + id;
		} else {
			return "" + id;
		}
	}
	public synchronized boolean getHighEst() {
		return highEst;
	}
	public synchronized void setHighEst(boolean highEst) {
		this.highEst = highEst;
	}


}

class ListenHandler extends Thread{
	Node nodeObj;
	ServerSocket listener;
	
	public ListenHandler(Node me) {
		
		nodeObj = me;
	}
	
	
	public void run(){
		try {
			
			listener = new ServerSocket(nodeObj.port);
			
			// continue accepting if all are establisged and 
			while (!nodeObj.getHighEst()) {

				new ListenerService(listener.accept(), nodeObj).start();
				for (int i = nodeObj.id + 1; i<= nodeObj.noOfNodes; i++){
					if(nodeObj.outMap.get(i)!=null){
						continue;						
					}
					else {
						nodeObj.setHighEst(true);
						
					}
				}
			}
		} 
		catch (IOException e) {
			
			e.printStackTrace();
		} 
		finally {
			try {
				listener.close();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}

class ListenerService extends Thread {

	Socket servSocket;
	Node n;
	String msg;
	BufferedReader is;

	public ListenerService(Socket csocket, Node n) {
		this.servSocket = csocket;
		this.n = n;
		is = null;
	}

	public void run() {

		try {
			System.out.println("in listener socket recd : " + servSocket);
			is = new BufferedReader(new InputStreamReader(servSocket.getInputStream()));
			
			int id = servSocket.getPort() - n.basePort;
			
			
			if(n.outMap.get(id)==null) //take its out stream
				n.outMap.put(id, new PrintWriter(servSocket.getOutputStream(),true));
			
			
			while ((msg = is.readLine()) != null) {

				if (msg.contentEquals("establish")) {
					n.setEst(true);;
					System.out.println("fuck off");

				}
				if (msg.startsWith("sent")) {
					System.out.println("yay");

				}
				if (msg.startsWith("hi")) {
					System.out.println("hi recieved from " + id);

				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
				servSocket.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}

		}
	}

}

class writingSocketThread extends Thread {

	Node n;
	String msg;
	int dstId;

	public writingSocketThread(Node n, int dstId, String msg) {

		this.n = n;
		this.dstId = dstId;
		this.msg = msg;
	}

	public void run() {
		try {

			int port = n.store.get(dstId).port;
			String host = n.store.get(dstId).host;
			InetAddress address = InetAddress.getByName(host);
			Socket dstSocket = new Socket(address, port);
			System.out.println("Sending socket" + dstSocket);
			PrintWriter out = new PrintWriter(dstSocket.getOutputStream(), true);
			out.println(msg);
			System.out.println("Sending" + msg);
			dstSocket.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
