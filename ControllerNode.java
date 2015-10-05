
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class ControllerNode {

	int id;
	String host;
	int noOfNodes = 10;
	int port;
	int basePort = 9000;
	String controllerHostName = "dc11.utdallas.edu";
	boolean init = true;
	boolean isListening = true;
	HashMap<Integer, Boolean> up = new HashMap<>();

	HashMap<Integer, NodeDef> store = new HashMap<Integer, NodeDef>();

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
		for (int i = 1; i < 12; i++) {

			store.put(i, new NodeDef(i, host, basePort + i));

		}

	}

	public static void main(String[] args) throws InterruptedException, IOException {

		ControllerNode me = new ControllerNode("11");

		me.initStore();
		System.out.println("Controller " + me + " is running.");

		Thread thread2 = new Thread(new Runnable() {
			public void run() {
				ServerSocket listener;

				try {
					listener = new ServerSocket(me.port);

					while (me.isListening) {
						System.out.println("ghoomte raho");
						Socket socket = listener.accept();

						Thread t1 = new Listeners(socket, me);
						t1.start();
						t1.join();
						if (me.up.size() != 1) {
							continue;
						} else if (me.up.size() == 1) {
							System.out.println("in breaker");
							me.isListening = false;
							for (int i = 1; i <= 1; i++) {
								new NotifyThreads(me, i, "establish", 0).start();
							}
							break;
						}

					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				finally {
				}

			}
		});
		thread2.start();
//		while (true) {
//			if (me.up.size() != 1) {
//				continue;
//			} else if (me.up.size() == 1) {
//				System.out.println("in breaker");
//				me.isListening = false;
//				for (int i = 1; i <= 1; i++) {
//					new NotifyThreads(me, i, "establish", 0).start();
//				}
//				break;
//			}
//
//		}

		System.out.println(me.up);

	}

}

class NotifyThreads extends Thread {

	ControllerNode n;
	int dstId;
	Object obj;

	public NotifyThreads(ControllerNode n, int dstId, Object obj, int type) {

		this.n = n;
		this.dstId = dstId;
		this.obj = obj;
	}

	public void run() {
		try {

			int port = n.store.get(dstId).port;
			String host = n.store.get(dstId).host;
			InetAddress address = InetAddress.getByName(host);
			Socket dstSocket = new Socket(address, port);
			PrintWriter out = new PrintWriter(dstSocket.getOutputStream(), true);

			// out.println((String)obj);
			out.println("establish");
			System.out.println("sent est to " + dstId);
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
	String msg;
	BufferedReader is;

	public Listeners(Socket csocket, ControllerNode n) {
		this.servSocket = csocket;
		this.n = n;
	}

	public void run() {

		try {

			is = new BufferedReader(new InputStreamReader(servSocket.getInputStream()));
			Thread.sleep(500);
			while ((msg = is.readLine()) != null) {
				msg = msg.split("#")[1];
				System.out.println("message recd : " + msg);
				int id = Integer.parseInt(msg);
				n.up.put(id, true);

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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
