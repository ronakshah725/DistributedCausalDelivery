package testQueue;

import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class QueueProcessor extends Thread {

	Node n;
	static Random r;
	BlockingQueue<BufferMessage> buffer = new ArrayBlockingQueue<>(500, true);

	boolean stopQueue =false;
	
	//data collection variables
	long averageBufferTime;
	int bufferedMessageCount = 0;
	int sentWithoutBufferingCount=0; 
	int maxMessagesBuffered = -1;
	

	public QueueProcessor(Node n) {
		r= new Random();
		this.n = n;
	}
	
	public synchronized boolean getStopQueue() {
		return stopQueue;
	}

	public synchronized void setStopQueue(boolean stopQueue) {
		this.stopQueue = stopQueue;
	}

	public void run() {
		Protocol m;
		while (!getStopQueue()) {
//			
			try {
				if (!n.queue.isEmpty()) {
					
					m = n.queue.take();
					if(m.type.startsWith("est")||m.type.startsWith("term"))
					{
						continue;
					}

					// check if the message can be delivered the node
					if (isDeliverable(n, m)) {
					// if yes, then consume the message and update node matrix
						deliver(n, m);
					} else {
						
						buffer.put(new BufferMessage(m, System.currentTimeMillis()));
						bufferedMessageCount++;
						System.out.println("buffer, current buff: " + buffer.size());
						//update maximum messages buffered
						maxMessagesBuffered = maxMessagesBuffered < buffer.size()? buffer.size():maxMessagesBuffered; 
					}
					if(n.getTerminate()){
						System.out.println("que terminate");
						break;
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void deliver(Node n2, Protocol m) {

		n2.componentViseUpdateMyMat( m);
		consume(n, m);
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
//		if(!buffer.isEmpty()){
//			//try to empty the queue
//			for (BufferMessage bm : buffer){ 
//				if(isDeliverable(n2, bm.m)){
//					deliverFromBuff(n2, bm); 
//					buffer.remove(bm);
//					System.out.println("removing");
//				}//recursive
//			}
//		}

	}

	/*
	 * OK
	 * OK
	 * 
	 * */
	
	private void deliverFromBuff(Node n2, BufferMessage bm) {
		n2.componentViseUpdateMyMat( bm.m);
		consume(n, bm.m);
		buffer.remove(bm.insertTS);
	}

	private void consume(Node n2, Protocol m) {
		//n2.recdMSGS += type + "#";
		n2.recdMSGS += " ["+m.id+"]:" + m.type + " #";
	}

	/*
	 * OK
	 * OK
	 * 
	 * */
	
	private boolean isDeliverable(Node n2, Protocol M) {

		int[][] p = n2.getMyMat();
		int[][] m = M.matrix;
		
		// nodes  from 1 to 10 but in matrix they are represented as 0 to 9
		int mid = M.id - 1;					
		int pid = n2.id - 1;
		
		//go through the column vector corresponding to this node is, in both recieved message vector and node's current matrix
		
		for (int i = 0; i < n2.noOfNodes; i++) {
											
			// process only column of node that is Node id ie pid
			int pb = p[i][pid];
			int mb = m[i][pid];

			//more than one difference means at least one message waiting to be delivered
			if (mb - pb > 1) 
				return false;
			
			//if recd comp is greater than node component by 1 and if 
			else if (mb - pb == 1 && mid != i) 
				return false;
		}
		return true;
	}

	/*
	 * OK
	 * OK
	 * 
	 * */
	static int getrandom(int min, int max) {
		return r.nextInt((max - min) + 1) + min;
	}
}

/*
 * OK
 * OK
 * 
 * */
@SuppressWarnings("serial")
class BufferMessage implements Serializable
{
	Protocol m;
	long insertTS;
	public BufferMessage(Protocol m, long insertTS) {
		this.m = m;
		this.insertTS = insertTS;
	}
}