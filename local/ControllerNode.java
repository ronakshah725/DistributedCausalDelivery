package local;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

public class ControllerNode {

	Integer NodeID;
	String HOSTNAME;
	int PORT = 9011;
	ServerSocket controllerSocket;

	SynchronousQueue<InMessage> q = new SynchronousQueue<>();

	static ConcurrentHashMap<Integer, PrintWriter> outMap = new ConcurrentHashMap<Integer, PrintWriter>();

	static ConcurrentHashMap<Integer, Boolean> checkStartMap = new ConcurrentHashMap<>();

	ControllerNode(int nodeID) {
		this.NodeID = nodeID;
		try {
			this.HOSTNAME = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String toString() {

		return NodeID + "@" + HOSTNAME + ":" + PORT;
	}

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub

		ControllerNode controller = new ControllerNode(11);
		try {

			controller.controllerSocket = new ServerSocket(controller.PORT);
			Thread t ;
			int upNodes;
			while (true) {
				System.out.println("Starting to accept");
				t = new controllerRequestHandler(controller.controllerSocket.accept());
				t.start();
				System.out.println(checkStartMap.size());
				if((upNodes = checkStartMap.size()) == 1){

					break;
				}
			}
			
			System.out.println("Out of while outMap: "+ outMap);
			System.exit(1);
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			while (!(upNodes == 10)) {
				// broadcast all nodes to start

				for (PrintWriter pw : outMap.values()) {

					pw.println("establish");
				}

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		finally {

			try {
				controller.controllerSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	@SuppressWarnings("unused")
	private static boolean isStopped() {
		// TODO Auto-generated method stub
		return false;
	}

	static class controllerRequestHandler extends Thread {

		private Integer clientNodeId;
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;

		public controllerRequestHandler(Socket cSocket) {
			this.socket = cSocket;
		}

		public void run() {
			try {
				System.out.println("In Request Handler for Client :" + socket);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				String inputString;

				while (true) {
					inputString = in.readLine();
					System.out.println("Value from client" + inputString);
					clientNodeId = Integer.parseInt(inputString.split("#")[0]);

					if (clientNodeId > 0 && clientNodeId < 11) {
						out.println("ok");
						System.out.println("client ok sent");
						break;

					}

				}
				System.out.println("Out of while");
				checkStartMap.put(clientNodeId, true);
				outMap.put(clientNodeId, out);

			} catch (IOException e) {
				System.out.println(e);
			} finally {
				
				try {
					socket.close();

					System.out.println("closed");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

	}

}
