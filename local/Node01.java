/*
 * 1)start node's listening thread to establish TCP to higher id nodes
 * 2)connect to controller and put its out in outMap
 * 3)controller makes sure all nodes up then informs all 
 * 
 * 
 * 
 * 
 * */
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

class InMessage {

	long ts;
	Integer NodeId;
	String msg;

	public InMessage(long ts, int nodeId, String msg) {

		this.ts = ts;
		NodeId = nodeId;
		this.msg = msg;
	}

}

public class Node01 {
	
	
	Integer NodeID;
	String HOSTNAME;
	int noOfNodes = 10;
	int PORT;
	int BasePort = 9000;
	String controllerHostName = "dc11.utdallas.edu";
	
	SynchronousQueue<InMessage> q = new SynchronousQueue<>();
	ConcurrentHashMap<Integer, PrintWriter> outMap = new ConcurrentHashMap<>();
	
	
	boolean startConnectingToOtherNodes = false;
	static boolean higherConnDone=false;

	Node01(int nodeID) {
		
		this.NodeID = nodeID;
		try {
			this.HOSTNAME = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PORT = BasePort + nodeID;

	}

	public String toString() {

		return NodeID + "@" + HOSTNAME + ":" + PORT;
	}
	
	 String getNodeAddress(int id) {
		if(id<10)
		{
		
			return "dc0"+id+".utdallas.edu";
		}
		else {
			return "dc"+id+".utdallas.edu";
		}

	}
	 

	public static void main(String[] args) throws IOException, InterruptedException {
////////////////////////////////////////////////////////////////////////////////////
		Node01 me = new Node01(01);
		System.out.println("Node" + me + " is running.");
		
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		
																   @SuppressWarnings("resource")
		ServerSocket listener = new ServerSocket(me.PORT);
		
		Thread.sleep(1000);
																	@SuppressWarnings("resource")
		Socket contSock = new Socket(me.HOSTNAME, 9011);
		PrintWriter outCont = new PrintWriter(contSock.getOutputStream(), true);
		BufferedReader inFromController = new BufferedReader(new InputStreamReader(contSock.getInputStream()));
		String inMSG;
		
		outCont.println(me.NodeID + "#" + "up");
		System.out.println("sent up");
		
		while ((inMSG = inFromController.readLine())!=null) {
			

			if (inMSG.contentEquals("ok")) {
				break;
			}

		}
		System.exit(1);
		
		
		
		
		
		
		
		
		
		
		
		
		
		Thread.sleep(1000);
		
		while(!me.startConnectingToOtherNodes){
			String msg = inFromController.readLine();
			if (msg.contains("establish")){
				me.startConnectingToOtherNodes = true;
			}	
		}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		while (!higherConnDone) {
			me.new RequestHandler(listener.accept()).start();
			if(me.outMap.size()==(me.noOfNodes-me.NodeID)) //for node 9 this will be 0, so it will exit while instantly
					{
						higherConnDone = true;
					}
					
		}

		
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
			if (me.startConnectingToOtherNodes) {  

				for (int id = me.NodeID-1; id > 0; id--) {
					me.new RequestHandler(new Socket(me.HOSTNAME, me.BasePort+id));
					

				}
			}
			
			System.out.println("Done connections to all");
			System.out.println(me.outMap);

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			

		



	}



	@SuppressWarnings("unused")
	private boolean isStopped() {
		// TODO Auto-generated method stub
		
		return false;
	}

	class RequestHandler extends Thread {

		private Integer clientNodeId; // id for which it is a handler

		private Socket socket;
		private BufferedReader in;

		public RequestHandler(Socket cSocket) {
			// TODO Auto-generated constructor stub\
			this.socket = cSocket;
			clientNodeId = (Integer) (cSocket.getPort() - 9000);
		}


		public void run() {
			try {

				System.out.println("In Request Handler for Client :" + clientNodeId);
				setIn(new BufferedReader(new InputStreamReader(socket.getInputStream())));
				
				outMap.put(clientNodeId, new PrintWriter(socket.getOutputStream(),true) );
				
				
		
			} catch (IOException e) {
				System.out.println(e);
			} finally {
	

			}
		}


		public BufferedReader getIn() {
			return in;
		}


		public void setIn(BufferedReader in) {
			this.in = in;
		}

	}

}
