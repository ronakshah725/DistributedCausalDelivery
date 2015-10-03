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

	ConcurrentHashMap<Integer, PrintWriter> outMap = new ConcurrentHashMap<Integer, PrintWriter>();
	
	
	ConcurrentHashMap<Integer, Boolean> checkStartMap = new ConcurrentHashMap<>();


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
	


	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ControllerNode controller = new ControllerNode(11);
		try {
			

			controller.controllerSocket= new ServerSocket(controller.PORT);;

			int upNodes;
			while ((upNodes = controller.checkStartMap.size()) != 10) {
				
				controller.new controllerRequestHandler(controller.controllerSocket.accept()).start();
			}
			
			while(upNodes != 10) {
				//broadcast all nodes to start
				
				for (PrintWriter pw : controller.outMap.values()){
		
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



	class controllerRequestHandler extends Thread {

		private Integer clientNodeId;
		private Socket socket;
		private BufferedReader in;


		public controllerRequestHandler(Socket cSocket) {
			this.socket = cSocket;
		}

		public void run() {
			try {

				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String inputString;
				while (true) {
					inputString=in.readLine();
					clientNodeId = Integer.parseInt(inputString.split("#")[0]);
					if (clientNodeId > 0 && clientNodeId < 11 && inputString.split("#")[1].contentEquals("up")) {
						break;

					}

				}
				checkStartMap.put(clientNodeId, true);
				outMap.put(clientNodeId, new PrintWriter(socket.getOutputStream()));

			} catch (IOException e) {
				System.out.println(e);
			} finally {

			}
		}

	}

}
