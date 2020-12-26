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

    //Statistics
    private int numComb = 0;
    private int numInvComb = 0;
    private int numValidComb = 0;
    private int avgCostValidComb = 0;
    private int avgScoreValidComb = 0;
    private JugadorsEquip bestCombination;
    private int bestScore = 0;
    private JugadorsEquip worseCombination;
    private int worseScore = 0;

    //Global Statistics
    public static int globalNumComb = 0;
    public static int globalNumInvComb = 0;
    public static int globalNumValidComb = 0;
    public static int globalAvgCostValidComb = 0;
    public static int globalAvgScoreValidComb = 0;
    public static JugadorsEquip globalBestCombination;
    public static int globalBestScore = 0;
    public static JugadorsEquip globalWorseCombination;
    public static int globalWorseScore = 0;

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

            numComb++;

            // Reject teams with repeated players.
            if (jugadors.JugadorsRepetits())
            {
                numInvComb++;
                //threadMessenger.addMessageToQueue(Error.color_red +" Invalid." + Error.end_color);
                continue;	// Equip no valid.
            }
            int costEquip = jugadors.CostEquip();
            int puntuacioEquip = jugadors.PuntuacioEquip();

            // Check if the team points is bigger than current optimal team, then evaluate if the cost is lower than the available budget
            checkTeam(equip, jugadors, this.PresupostFitxatges, costEquip, puntuacioEquip);

            //Calculate statistics
            CalculateStatistics(jugadors, costEquip, puntuacioEquip);

            if (numComb%M == 0)
                printStatistics();

        }

        //Wait for threads; print global statistics; send signal to parent
        try{
            evaluatorLock.lock();
            threadsFinished++;
            //Wait for the others
            while (threadsFinished < numOfThreads)
                evaluatorFinished.await();
            printStatistics();
            calculateGlobalStatistics();
            //Last thread prints global and sends signal to parent
            if (threadsFinished == numOfThreads*2 - 1){
                printGlobalStatistics();
                threadMessenger.forcePrint();
                evaluatorsEnded.signalAll();
            }
            threadsFinished++;
            evaluatorFinished.signal();
        } catch (java.lang.InterruptedException exception) {
            System.out.println("Program Interrupted");
        } finally {
            evaluatorLock.unlock();
        }
    }

    private void printStatistics(){
        threadMessenger.addMessageToQueue(Error.color_green + "*******THREAD " + Thread.currentThread().getId()+ " STATISTICS******"+
                "\nNúmero de Combinaciones evaluadas: " + numComb +
                "\nNúmero de combinaciones no válidas: " + numInvComb +
                "\nCoste promedio de las combinaciones válidas: " + avgCostValidComb +
                "\nPuntuación promedio de las combinaciones válidas: " + avgScoreValidComb +
                "\nMejor combinación (desde el punto de vista de la puntuación): " + bestCombination.toStringEquipJugadors() +
                "\nPeor combinación (desde el punto de vista de la puntuación): " + worseCombination.toStringEquipJugadors() +
                "\n********************************************************" + Error.end_color);
    }

    public void printGlobalStatistics(){
        threadMessenger.addMessageToQueue(Error.color_blue + "*******GLOBAL STATISTICS******"+
                "\nNúmero de Combinaciones evaluadas: " + globalNumComb +
                "\nNúmero de combinaciones no válidas: " + globalNumInvComb +
                "\nCoste promedio de las combinaciones válidas: " + globalAvgCostValidComb +
                "\nPuntuación promedio de las combinaciones válidas: " + globalAvgScoreValidComb +
                "\nMejor combinación (desde el punto de vista de la puntuación): " + globalBestCombination.toStringEquipJugadors() +
                "\nPeor combinación (desde el punto de vista de la puntuación): " + globalWorseCombination.toStringEquipJugadors() +
                "\n********************************************************" + Error.end_color);
    }

    public void calculateGlobalStatistics(){
        globalNumComb += numComb;
        globalNumInvComb += numInvComb;
        globalNumValidComb += numValidComb;
        globalAvgCostValidComb = ((globalAvgCostValidComb * globalNumValidComb) + (avgCostValidComb * numValidComb)) / globalNumValidComb;
        globalAvgScoreValidComb = ((globalAvgScoreValidComb * globalNumValidComb) + (avgScoreValidComb * numValidComb)) / globalNumValidComb;
        if (bestScore > globalBestScore){    //Best combination regarding points
            globalBestScore = bestScore;
            globalBestCombination = bestCombination;
        }else if (worseScore < globalWorseScore || globalWorseScore == 0) {   //Worse combination regarding points
            globalWorseScore = worseScore;
            globalWorseCombination = worseCombination;
        }
    }

    private void CalculateStatistics(JugadorsEquip jugadors, int costEquip, int puntuacioEquip) {
        avgCostValidComb = ((avgCostValidComb * numValidComb) + costEquip) / (numValidComb+1);
        avgScoreValidComb = ((avgScoreValidComb * numValidComb) + puntuacioEquip) / (numValidComb+1);
        numValidComb++;
        if (puntuacioEquip > bestScore){    //Best combination
            bestScore = puntuacioEquip;
            bestCombination = jugadors;
        }else if (puntuacioEquip < worseScore || worseScore == 0) {   //Worse combination
            worseScore = puntuacioEquip;
            worseCombination = jugadors;
        }
    }

    //Synchronized so it doesn't change value between checking the value and changing
    public static synchronized void checkTeam(int equip, JugadorsEquip jugadors, int PresupostFitxatges, int costEquip, int puntuacioEquip){
        if (puntuacioEquip > threadEvaluator.MaxPuntuacio && costEquip < PresupostFitxatges)
        {
            threadMessenger.addMessageToQueue("Thread: " + Thread.currentThread().getId() + " Team " + equip + "->" + Error.color_green + " Cost: " + costEquip + " Points: " + puntuacioEquip + ". "+ Error.end_color);
            // We have a new partial optimal team.
            threadEvaluator.MaxPuntuacio=puntuacioEquip;
            threadEvaluator.MillorEquip = jugadors;
        }
        else
        {
            threadMessenger.addMessageToQueue("Cost: " + costEquip + " Points: " + puntuacioEquip + ".\r");
        }
    }

    public int getEnd() { return end; }
}



//System.out.println(Error.color_red +" Invalid." + Error.end_color);
//System.out.print("Thread: " + Thread.currentThread().getId() + " Team " + equip + "->");
//threadMessenger.addMessageToQueue(Error.color_green + " Cost: " + jugadors.CostEquip() + " Points: " + jugadors.PuntuacioEquip() + ". "+ Error.end_color);
//System.out.println(Error.color_green + " Cost: " + jugadors.CostEquip() + " Points: " + jugadors.PuntuacioEquip() + ". "+ Error.end_color);
//System.out.println(" Cost: " + jugadors.CostEquip() + " Points: " + jugadors.PuntuacioEquip() + ".\r");