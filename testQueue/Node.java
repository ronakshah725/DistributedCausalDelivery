package testQueue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Node {

	public static void main(String[] args) throws IOException, InterruptedException {

		Node me = new Node(args[0]);
		System.out.println("Node " + me + " is running.");
		
		me.store.put(11, new NodeDef(11, me.controllerHostName, me.basePort+11));
		
		String nodeInfo = me.id +"sp"+ me.host + "sp"+ me.port;
		Protocol p = new Protocol(System.currentTimeMillis(), me.id, me.myMat, "up" + "#" + nodeInfo);
		new writingSocketThread(me, 11, p).start();
		new ListenHandler(me).start();
		Thread.sleep(2000); // wait for all to be up
		while (!me.getEst()) {
		}
		System.out.println("All nodes up");
		System.out.println(me.store);

		
		//start processing from the queue
		me.qp = new QueueProcessor(me);
		me.qp.start();

		// send 100 messages
		while (!(me.messagesSent == 20)) {
			System.out.print("sending message :");
			Thread.sleep(getrandom(20, 100));
			me.emitMessage();
			me.messagesSent++;

		}
		
		//todo terminate signal to controller
		new writingSocketThread(me, 11, new Protocol(System.currentTimeMillis(), me.id, me.myMat, "down" + "#" + getIDString(me.id) )).start();

		System.out.println("sent terminate signal");
		
		while (!me.getTerminate()) {
		}
		
		System.out.println("Recieved messages : " + me.recdMSGS);
		System.out.println("All  dead! Fuck OFF");
		
		me.qp.setStopQueue(true);

	}

	private void emitMessage() {

//		int x = getrandom(1, 9);
		int x = getrandom(1, 2);
		
		
		//multicast to x destinations
		
		for (int i = 1; i <= x; i++) {
			
			//for each destination
			int dstID;
			
			// randomly select a node
			while (true) {
//				dstID = getrandom(1, 10);
				dstID = getrandom(1, 3);
				//other than itself
				if (dstID != this.id)
					break;
			}
			
			
			// for node id to matrix mapping [INTERNALS]
			dstID = dstID -1;
			int srcID = this.id-1;
			
			
			//update myMat
			
			//get
			int myMat[][] = this.getMyMat();
			//update src dst in matrix
			System.out.println("s: " + srcID +", d: "+ dstID);
			myMat[srcID][dstID]++; 
			//update myMat 
			this.myMatSendUpdate(myMat);
			
			//generate protocol message
			Protocol p = new Protocol(System.currentTimeMillis(), this.id, this.getMyMat(),  this.messagesSent + "");
			
			//send
			
			dstID =dstID + 1;
			new writingSocketThread(this, dstID, p).start();
			
			
		}

	}

	static int getrandom(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}




	public synchronized void setEst(boolean est) {
		this.established = est;
	}

	public synchronized boolean getEst() {
		return this.established;
	}

	synchronized int[][] getMyMat() {

		return this.myMat;
	}

	synchronized void componentViseUpdateMyMat( Protocol M) {
		
		int[][] p = this.getMyMat();
		int[][] m = M.matrix;

		for (int i = 0; i < this.noOfNodes; i++)
			for (int j = 0; j < this.noOfNodes; j++) {
				p[i][j] = m[i][j] > p[i][j] ? m[i][j] : p[i][j];
			}

	}

	synchronized void myMatSendUpdate(int[][] newMyMat) {
		this.myMat = newMyMat;
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

	
	public synchronized boolean getTerminate() {
		return terminate;
	}

	public synchronized void setTerminate(boolean terminate) {
		this.terminate = terminate;
	}

	Node(String id) {

		this.id = Integer.parseInt(id);
		try {
			this.host = InetAddress.getLocalHost().getHostName();
			this.controllerHostName = this.host;
		} catch (UnknownHostException e) {

			e.printStackTrace();
		}
		this.port = this.basePort + this.id;
		this.myMat = new int[this.noOfNodes][this.noOfNodes];
		for (int i = 0; i < this.noOfNodes; i++) {
			for (int j = 0; j < this.noOfNodes; j++) {
				this.myMat[i][j] = 0;
			}
		}
		this.recdMSGS = "Recieved Messages at " + getIDString(this.id) + " :" + "#";

	}

	
	int id;
	String host;
	final int noOfNodes = 3;
	int port;
	int basePort = 9000;
//	String controllerHostName = "dc11.utdallas.edu";
	String controllerHostName ;
	boolean established = false;
	boolean terminate = false;

	int[][] myMat;
	String recdMSGS; // from each nod
	int messagesSent = 0;

	BlockingQueue<Protocol> queue = new ArrayBlockingQueue<Protocol>(500);

	ConcurrentHashMap<Integer, NodeDef> store = new ConcurrentHashMap<Integer, NodeDef>();
	
	QueueProcessor qp;
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
			while (!nodeObj.terminate) {

				System.out.println("In listener");
				Socket socket = listener.accept();

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
			
			ObjectInputStream iis = new ObjectInputStream(servSocket.getInputStream());
			@SuppressWarnings("unused")
			int id = servSocket.getLocalPort() - n.basePort;
			while (true) {
				Protocol msg;
				msg = (Protocol) iis.readObject();
				//System.out.println("msg recd : " + msg);
				if (msg == null)
					break;

				n.queue.put(msg);
				if (msg.type.startsWith("est")) {
					//todo take nodestore
					String[] nodestore = msg.type.split("sp");
					for(int i=1; i<nodestore.length; i=i+3){
						NodeDef ndef = new NodeDef(Integer.parseInt(nodestore[i]), nodestore[i+1], Integer.parseInt(nodestore[i+2]));
						n.store.put(Integer.parseInt(nodestore[i]), ndef);
						
					}
					n.setEst(true);
					System.out.println("est recd");
				}
				else if(msg.type.startsWith("term")){
					n.setTerminate(true);
					System.out.println("term recd");
					
					
					//potential place for breaking listener
					break;
					
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
			System.out.println("sending to " + host +":" + port);
			InetAddress address = InetAddress.getByName(host);
			Socket dstSocket = new Socket(address, port);
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
