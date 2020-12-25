package eps.udl.cat;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class threadMessenger extends Thread{
    private static final int LIST_SIZE = 100;
    private static final LinkedList<String> list = new LinkedList<>();
    public static final ReentrantLock lock = new ReentrantLock();
    public static final Condition listFull = lock.newCondition();
    public static final Condition itemAdded = lock.newCondition();


    private void printMessages(){
        for (String s : list) {
            System.out.println(s);
        }
        list.clear();
    }

    public static synchronized void addMessageToQueue(String message) {
        try {
            lock.lock();
            //If messenger thread is printing the messages in the list/queue, wait
            while (list.size() >= LIST_SIZE)
                listFull.await();
            list.add(message);
            itemAdded.signalAll();
        } catch (java.lang.InterruptedException exception) {
            System.out.println("Program Interrupted");
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run(){
        while (true){
            try{
                lock.lock();
                //Wait until list is full
                while (list.size() < LIST_SIZE)
                    itemAdded.await();
                printMessages();
                listFull.signalAll();
            } catch (java.lang.InterruptedException exception) {
                System.out.println("Program Interrupted");
            } finally {
                lock.unlock();
            }
        }
    }
}
