package backups;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Node {

	public static void main(String[] args) throws IOException, InterruptedException {

		Node me = new Node(args[0]);
		System.out.println("Node " + me + " is running.");
		me.initStore();
		Protocol p = new Protocol(System.currentTimeMillis(), me.id, me.myMat, "up" + "#" + getIDString(me.id));
		new writingSocketThread(me, 11, p ).start();
		new ListenHandler(me).start();
		Thread.sleep(2000); // wait for all to be up
		while (!me.getEst()) {
		}
		System.out.println("est : " + me.getEst());
		// lets do a broadcast
//		for (int i = 1; i <= me.noOfNodes; i++) {
//			if (i == me.id)
//				continue;
//			System.out.println("in broadcast");
//			new writingSocketThread(me, i, "hi from " + i).start();
//		}
		new QueueProcessor(me).start();
	}

	public void initStore() {
		for (int i = 1; i <= 11; i++) {
			store.put(i, new NodeDef(i, host, basePort + i));
		}
	}

	public synchronized void setEst(boolean est) {
		established = est;
	}

	public synchronized boolean getEst() {
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
		this.myMat = new int[10][10];
		for(int i = 0; i<10; i++){
			for (int j = 0; j<10; j++ ){
				this.myMat[i][j] = 0;
			}
		}

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

	int id;
	String host;
	final int noOfNodes = 2;
	int port;
	int basePort = 9000;
	String controllerHostName = "dc11.utdallas.edu";
	boolean established = false;
	int [][] myMat;
	
	BlockingQueue<Protocol> queue = new ArrayBlockingQueue<Protocol>(100);

	HashMap<Integer, NodeDef> store = new HashMap<Integer, NodeDef>();
}

class QueueProcessor extends Thread {
	Node n;

	public QueueProcessor(Node n) {
		this.n = n;
	}

	public void run() {
		try {
			Protocol m;
			while ((m = n.queue.take()) != null) {
				System.out.println("Queue : " + m);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

class ListenHandler extends Thread {
	Node nodeObj;
	ServerSocket listener;

	public ListenHandler(Node me) {
		nodeObj = me;
	}

	public void run() {
		try {

			listener = new ServerSocket(nodeObj.port);
			while (true) {

				System.out.println("in listener");
				Socket socket = listener.accept();
				System.out.println(socket);
				new ListenerService(socket, nodeObj).start();
			}
		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			try {
				listener.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}

class ListenerService extends Thread {
	Socket servSocket;
	Node n;
	BufferedReader is;

	public ListenerService(Socket csocket, Node n) {
		this.servSocket = csocket;
		this.n = n;
		is = null;
	}

	public void run() {
		try {
			System.out.println("in listener socket recd : " + servSocket);
			ObjectInputStream iis = new ObjectInputStream(servSocket.getInputStream());
			System.out.println(iis);
			@SuppressWarnings("unused")
			int id = servSocket.getLocalPort() - n.basePort;
			while (true) {
				Protocol msg;
				msg = (Protocol)iis.readObject();
				if (msg == null)
					break;
				System.out.println("ghoomte raho in listener service");
				n.queue.put(msg);
				if ( msg.type.startsWith("establish")) {
					n.setEst(true);
					System.out.println("fuck off");
				}
				if (msg.type.startsWith("sent")) {
					System.out.println("yay");
				}
			}
		} catch (IOException e) {
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				servSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

class writingSocketThread extends Thread {
	Node n;
	int dstId;
	Protocol p;

	public writingSocketThread(Node n, int dstId, Protocol p) {

		this.n = n;
		this.dstId = dstId;
		this.p = p;
	}



	public void run() {
		try {

			int port = n.store.get(dstId).port;
			String host = n.store.get(dstId).host;
			InetAddress address = InetAddress.getByName(host);
			Socket dstSocket = new Socket(address, port);
			System.out.println("Sending socket" + dstSocket);
			ObjectOutputStream oos = new ObjectOutputStream(dstSocket.getOutputStream());
			oos.writeObject(p);
			dstSocket.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
