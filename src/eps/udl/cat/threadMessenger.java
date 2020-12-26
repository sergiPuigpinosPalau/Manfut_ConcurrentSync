package eps.udl.cat;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class threadMessenger extends Thread{
    private static final int LIST_SIZE = 100;
    private static final LinkedList<String> messageList = new LinkedList<>();
    public static final ReentrantLock messengerLock = new ReentrantLock();
    public static final Condition itemAdded = messengerLock.newCondition();
    public static final Semaphore semaphore = new Semaphore(LIST_SIZE);
    public static boolean bForcePrint = false;


    private void printMessages(){
        for (String s : messageList) {
            System.out.println(s);
        }
        messageList.clear();
    }

    public static void addMessageToQueue(String message) {
        try {
            semaphore.acquire();
            messengerLock.lock();
            messageList.add(message);
            itemAdded.signalAll();
        } catch (java.lang.InterruptedException exception) {
            System.out.println("Program Interrupted");
        } finally {
            messengerLock.unlock();
        }
    }

    public static void forcePrint(){
        //Lock to avoid changing the boolean while the messenger thread is resetting its value
        try {
            messengerLock.lock();
            bForcePrint=true;
            itemAdded.signalAll();
        } finally {
            messengerLock.unlock();
        }
    }

    @Override
    public void run(){
        while (true){
            try{
                messengerLock.lock();
                //Wait until list is full
                while (messageList.size() != LIST_SIZE && !bForcePrint)
                    itemAdded.await();
                int remaining = semaphore.availablePermits();   //In case of a forcePrint
                printMessages();
                semaphore.release(LIST_SIZE-remaining);
            } catch (java.lang.InterruptedException exception) {
                System.out.println("Program Interrupted");
            } finally {
                bForcePrint=false;
                messengerLock.unlock();
            }
        }
    }
}
