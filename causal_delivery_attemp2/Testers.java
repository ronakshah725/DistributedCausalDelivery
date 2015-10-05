package causal_delivery_attemp2;

public class Testers {

	public static void main(String[] args) {

        Thread thread2 = new Thread(new Runnable() {
            public void run() {
                for(int i = 0; i < 10000; i++) {
                    System.out.println("hi");
                }
            }
        });
        thread2.start();
		
		
	}

}
