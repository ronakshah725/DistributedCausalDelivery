package backups;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Node {

	int id;
	String host;
	final int noOfNodes = 2;
	int port;
	int basePort = 9000;
	String controllerHostName = "dc11.utdallas.edu";
	boolean established = false;
	 BlockingQueue<String> queue = new ArrayBlockingQueue<String>(10);

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
		//lets do a broadcast
//		for (int i = 1; i<=me.noOfNodes; i++){
//			if(i==me.id)continue;
//			System.out.println("in broadcast");
			new writingSocketThread(me, 1, "hi from " + 1).start();;
		
		
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


}

class QueueProcessor extends Thread{
	
	Node n;
	public QueueProcessor(Node n) {
		this.n = n;
		
	}
	
	public void run(){
		try {

			System.out.println(n.queue.take());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
			while (true) {

				System.out.println("in listener");
				Socket socket = listener.accept();
				System.out.println(socket);
				new ListenerService(socket, nodeObj).start();
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
			while ((msg = is.readLine()) != null) {
				System.out.println("ghoomte raho in listener service");
				n.queue.put(msg);
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
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
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
