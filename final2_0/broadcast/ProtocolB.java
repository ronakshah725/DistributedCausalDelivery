//message format for broadcast



import java.io.Serializable;


@SuppressWarnings("serial")
public class ProtocolB implements Serializable {

	long ts;
	int id;
	int [] matrix = new int[10];
	String type;
	
	public ProtocolB(long ts, int id, int[] matrix, String type) {
		this.ts = ts;
		this.id = id;
		this.matrix = matrix;
		this.type = type;
	}

public String toString(){
	
	return "ID:"+ id+", " +"TimeStamp:" + ts+ ", " +"Type:"+ type + "\n" ;
//	return "ID:"+ id+", " +"TimeStamp:" + ts+ ", " +"Type:"+ type + "\n"  ;

}


	
	

}


