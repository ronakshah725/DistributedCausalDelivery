package testQueue;

import java.util.Random;

public class QueueProcessor extends Thread {

	Node n;
	static Random r;

	public QueueProcessor(Node n) {
		this.n = n;
	}

	public void run() {
		Protocol m;
		while (!n.terminate) {
			try {
				// mimic propogation delay
				Thread.sleep((long)getrandom(50, 100));
				m = n.queue.take();
				
				//check if the message can be delivered the node
				if(isDeliverable(n,m)){
					
					
				//if yes, then consume the message and update node matrix
					Deliver(n,m);
				}
				
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}

	
	private void Deliver(Node n2, Protocol m) {
		// TODO Auto-generated method stub
		updateMyMat(n2, m);
		consume(n, m.type);//append to some string
		
	}

	private void consume(Node n2, String type) {
		n2.recdMSGS += type + "#"; 
		
	}

	private void updateMyMat(Node n2, Protocol M) {
		// TODO Auto-generated method stub
		int[][] p = n2.myMat;
		int[][]m = M.matrix;
		
		
		for(int i=0; i< n2.noOfNodes; i++)
			for(int j = 0; j<n2.noOfNodes; j++){
				p[i][j]=m[i][j]>p[i][j]?m[i][j]:p[i][j] ;
			}
		
	}

	private boolean isDeliverable(Node n2, Protocol M) {
		int[][] p = n2.myMat;
		int[][]m = M.matrix;
		
		
		int mid = M.id;
		int pid = n2.id;
		for(int i=0; i< n2.noOfNodes;i++)
		{
				//process only column of node that is pid
				int pb = p[i][pid];
				int mb = m[i][pid];
				
				if(mb-pb>1)
					return false;
				else 
					if(mb-pb==1 && mid!=i){
						return false;
					}
				return true;
				
			}
		return false;
	}

	static int getrandom(int min, int max) {
		return r.nextInt((max - min) + 1) + min;

	}
}


