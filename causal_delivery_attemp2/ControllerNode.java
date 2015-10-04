package causal_delivery_attemp2;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

public class ControllerNode {
	
	int id;
	String host;
	int noOfNodes = 10;
	int port;
	int basePort = 9000;
	String controllerHostName = "dc11.utdallas.edu";
	
	HashMap<Integer,NodeDef> store = new HashMap<Integer,NodeDef>();
	ControllerNode(String id) {
		
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
		
		ControllerNode me = new ControllerNode("11");
		System.out.println("Controller " + me + " is running.");
		
	}
	

}
