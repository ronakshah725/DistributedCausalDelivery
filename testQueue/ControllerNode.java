package testQueue;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class ControllerNode {

	public static void main(String[] args) throws InterruptedException, IOException {
		ControllerNode me = new ControllerNode("11");
		me.initStore();
		System.out.println("Controller " + me + " is running.");
		ServerSocket listener = new ServerSocket(me.port);
		try {
			while (me.isListening) {
				System.out.println("ghoomte raho");
				Socket socket = listener.accept();
				Thread t1 = new Listeners(socket, me);
				t1.start();
				t1.join();
			}
			if (me.up.size() == me.noOfNodes) {
				System.out.println("in breaker");
				for (int i = 1; i <= me.noOfNodes; i++) {
					Protocol p = new Protocol(System.currentTimeMillis(), me.id, new int[10][10], "establish");
					new NotifyThreads(me, i, p, 0).start();
				}
			} else {
				System.out.println(me.up);
			}
		} finally {
			listener.close();
		}
	}

	ControllerNode(String id) {
		this.id = Integer.parseInt(id);
		try {
			this.host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			System.out.println("Unknown Host");
			e.printStackTrace();
		}
		this.port = this.basePort + this.id;
	}

	public String toString() {
		return id + "@" + host + ":" + port;
	}

	public void initStore() {
		// take from config file
		for (int i = 1; i <= noOfNodes + 1; i++) {
			store.put(i, new NodeDef(i, host, basePort + i));
		}
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
	boolean init = true;
	boolean isListening = true;
	HashMap<Integer, Boolean> up = new HashMap<>();

	HashMap<Integer, NodeDef> store = new HashMap<Integer, NodeDef>();

}

class NotifyThreads extends Thread {

	ControllerNode n;
	int dstId;
	Protocol obj;

	public NotifyThreads(ControllerNode n, int dstId, Protocol obj, int type) {

		this.n = n;
		this.dstId = dstId;
		this.obj = obj;
	}

	public void run() {
		try {
			System.out.println("in notify");
			int port = n.store.get(dstId).port;
			String host = n.store.get(dstId).host;
			InetAddress address = InetAddress.getByName(host);
			Socket dstSocket = new Socket(address, port);
			System.out.println("Sending socket" + dstSocket);
			ObjectOutputStream oos = new ObjectOutputStream(dstSocket.getOutputStream());
			oos.writeObject(obj);
			System.out.println("sent est");
			oos.close();
			dstSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class Listeners extends Thread {

	Socket servSocket;
	ControllerNode n;
	ObjectInputStream iis;

	public Listeners(Socket csocket, ControllerNode n) {

		this.servSocket = csocket;
		this.n = n;
	}

	public void run() {

		try {
			iis = new ObjectInputStream(servSocket.getInputStream());
			Thread.sleep(500);
			if ((n.init == true)) // not init phase
			{
				Protocol msg = (Protocol) iis.readObject();
				msg.type = msg.type.split("#")[1];
//				System.out.println("message recd : " + msg);
				int id = Integer.parseInt(msg.type);
				n.up.put(id, true);
				System.out.println("up");
				if (n.up.size() == n.noOfNodes) {
					n.isListening = false;
				}
			} else if (n.init == false) {

			}
		} catch (IOException e) {
			e.printStackTrace();
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
