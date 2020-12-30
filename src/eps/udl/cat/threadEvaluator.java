/* ---------------------------------------------------------------
Práctica 1.
Código fuente: threadEvaluater.java
Grau Informàtica
49259953W i Sergi Puigpinós Palau.
47694432E i Jordi Lazo Florensa.
--------------------------------------------------------------- */
package eps.udl.cat;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class threadEvaluator extends Thread{

    private final int first;
    private final int end;
    private final int PresupostFitxatges;
    private final Market market;
    private final int M;
    private final int numOfThreads;

    //private static boolean isNull = false;
    public static JugadorsEquip MillorEquip = null;
    public static int MaxPuntuacio = -1;
    public static int threadsFinished = 0;
    //public static threadMessenger messenger;

    private final Statistics statistics = new Statistics();
    public static Statistics globalStatistics = new Statistics();

    //Locks
    public static final ReentrantLock evaluatorLock = new ReentrantLock();
    public static final Condition evaluatorFinished = evaluatorLock.newCondition();
    public static final Condition evaluatorsEnded = evaluatorLock.newCondition();


    threadEvaluator(int first, int end, int PresupostFitxatges, Market market, int M, int numOfThreads){
        this.first = first;
        this.end = end;
        this.market = market;
        this.PresupostFitxatges = PresupostFitxatges;
        this.M = M;
        this.numOfThreads = numOfThreads;
    }

    @Override
    public void run(){
        int equip;
        for (equip=first;equip<=end;equip++)
        {
            JugadorsEquip jugadors;

            // Get playes from team number. Returns false if the team is not valid.
            if ((jugadors=market.ObtenirJugadorsEquip(new IdEquip(equip)))==null)
                continue;

            statistics.incrementNumComb();

            // Reject teams with repeated players.
            if (jugadors.JugadorsRepetits())
            {
                statistics.incrementNumInvComb();
                //threadMessenger.addMessageToQueue(Error.color_red +" Invalid." + Error.end_color);
                continue;	// Equip no valid.
            }
            int costEquip = jugadors.CostEquip();
            int puntuacioEquip = jugadors.PuntuacioEquip();

            // Check if the team points is bigger than current optimal team, then evaluate if the cost is lower than the available budget
            checkTeam(equip, jugadors, this.PresupostFitxatges, costEquip, puntuacioEquip);

            //Calculate statistics
            statistics.calculateStatistics(jugadors, costEquip, puntuacioEquip);

            if (statistics.getNumComb()%M == 0)
                statistics.printStatistics();

        }

        //Wait for threads; print global statistics; send signal to parent
        try{
            evaluatorLock.lock();
            threadsFinished++;
            //Wait for the others
            while (threadsFinished < numOfThreads)
                evaluatorFinished.await();
            statistics.printStatistics();
            threadEvaluator.globalStatistics.calculateGlobalStatistics(statistics);
            //Last thread prints global and sends signal to parent
            if (threadsFinished == numOfThreads*2 - 1){
                threadEvaluator.globalStatistics.printGlobalStatistics();
                evaluatorsEnded.signalAll();
            } else {
                threadsFinished++;
                evaluatorFinished.signal();
            }
        } catch (java.lang.InterruptedException exception) {
            System.out.println("Program Interrupted");
        } finally {
            evaluatorLock.unlock();
        }
    }

    //Synchronized so it doesn't change value between checking the value and changing
    public static synchronized void checkTeam(int equip, JugadorsEquip jugadors, int PresupostFitxatges, int costEquip, int puntuacioEquip){
        if (puntuacioEquip > threadEvaluator.MaxPuntuacio && costEquip < PresupostFitxatges)
        {
            threadMessenger.addMessageToQueue(Error.color_green + "Thread: " + Thread.currentThread().getId() + " Team " + equip + " -> " + " Cost: " + costEquip + " Points: " + puntuacioEquip + ". "+ Error.end_color);
            // We have a new partial optimal team.
            threadEvaluator.MaxPuntuacio=puntuacioEquip;
            threadEvaluator.MillorEquip = jugadors;
        }
        else
        {
            threadMessenger.addMessageToQueue("Thread: " + Thread.currentThread().getId() + " Team " + equip + " -> " + "Cost: " + costEquip + " Points: " + puntuacioEquip + ".\r");
        }
    }

    public int getEnd() { return end; }
}
