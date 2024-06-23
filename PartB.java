package ProjectB;

//Data class to hold shared variables and synchronization flags
class Data {
 int A1, A2, A3, B1, B2, B3;  // Shared variables for calculations
 boolean goFuncA1 = false;    // Flags to control the synchronization flow
 boolean goFuncA2 = false;
 boolean goFuncA3 = false;
 boolean goFuncB1 = false;
 boolean goFuncB2 = false;
 boolean goFuncB3 = false;
}

public class PartB {

 public static void main(String[] args) throws InterruptedException {
     Data sample = new Data();  // Create an instance of the Data class

     int test_size = 10;  // Number of test iterations
     for (int i = 1; i <= test_size; i++) {
         System.out.println("Loop " + i + "\n");

         ThreadA ta = new ThreadA(sample);
         ThreadB tb = new ThreadB(sample);
         ta.start();
         tb.start();
         ta.join();  // Wait for ThreadA to finish
         tb.join();  // Wait for ThreadB to finish

         System.out.println("\n");
     }
 }
}

//ThreadA class to perform calculations and synchronize with ThreadB
class ThreadA extends Thread {
 private Data sample;
 public ThreadA(Data sample) {
     this.sample = sample;  // Initialize with shared Data instance
 }

 public void run() {
     synchronized (sample) {
         int n = 500;
         sample.A1 = n * (n + 1) / 2;  // Calculate A1
         System.out.println("A1 is: " + sample.A1);
         sample.goFuncB2 = true;  // Signal ThreadB to proceed to B2
         sample.notify();  // Notify any waiting thread
     }

     synchronized (sample) {
         while (!sample.goFuncA2) {
             System.out.println("Waiting for B2 to complete to proceed to A2...");
             try {
                 sample.wait();  // Wait until notified
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
             }
         }
         int i = 300;
         sample.A2 = sample.B2 + (i * (i + 1)) / 2;  // Calculate A2
         System.out.println("A2 is: " + sample.A2);
         sample.goFuncB3 = true;  // Signal ThreadB to proceed to B3
         sample.notify();  // Notify any waiting thread
     }

     synchronized (sample) {
         while (!sample.goFuncA3) {
             System.out.println("Waiting for B3 to complete to proceed to A3...");
             try {
                 sample.wait();  // Wait until notified
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
             }
         }
         int n = 400;
         sample.A3 = sample.B3 + (n * (n + 1)) / 2;  // Calculate A3
         System.out.println("A3 is: " + sample.A3);
         sample.notify();  // Notify any waiting thread
     }
 }
}

//ThreadB class to perform calculations and synchronize with ThreadA
class ThreadB extends Thread {
 private Data my_sample;
 public ThreadB(Data my_sample) {
     this.my_sample = my_sample;  // Initialize with shared Data instance
 }

 public void run() {
     synchronized (my_sample) {
         int n = 250;
         my_sample.B1 = n * (n + 1) / 2;  // Calculate B1
         System.out.println("B1 is: " + my_sample.B1);
     }

     synchronized (my_sample) {
         while (!my_sample.goFuncB2) {
             System.out.println("Waiting for A1 to complete to proceed to B2...");
             try {
                 my_sample.wait();  // Wait until notified
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
             }
         }
         int z = 200;
         my_sample.B2 = my_sample.A1 + z * (z + 1) / 2;  // Calculate B2
         System.out.println("B2 is: " + my_sample.B2);
         my_sample.goFuncA2 = true;  // Signal ThreadA to proceed to A2
         my_sample.notify();  // Notify any waiting thread
     }

     synchronized (my_sample) {
         while (!my_sample.goFuncB3) {
             System.out.println("Waiting for A2 to complete to proceed to B3...");
             try {
                 my_sample.wait();  // Wait until notified
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
             }
         }
         int y = 400;
         my_sample.B3 = my_sample.A2 + y * (y + 1) / 2;  // Calculate B3
         System.out.println("B3 is: " + my_sample.B3);
         my_sample.goFuncA3 = true;  // Signal ThreadA to proceed to A3
         my_sample.notify();  // Notify any waiting thread
     }
 }
}
