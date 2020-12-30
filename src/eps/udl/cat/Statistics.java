package eps.udl.cat;

public class Statistics {
    private int numComb;
    private int numInvComb;
    private int numValidComb;
    private int avgCostValidComb;
    private int avgScoreValidComb;
    private JugadorsEquip bestCombination;
    private int bestScore;
    private JugadorsEquip worseCombination;
    private int worseScore = -1;


    public void calculateStatistics(JugadorsEquip jugadors, int costEquip, int puntuacioEquip) {
        avgCostValidComb = ((avgCostValidComb * numValidComb) + costEquip) / (numValidComb+1);
        avgScoreValidComb = ((avgScoreValidComb * numValidComb) + puntuacioEquip) / (numValidComb+1);
        numValidComb++;
        if (puntuacioEquip > bestScore){    //Best combination
            bestScore = puntuacioEquip;
            bestCombination = jugadors;
        }else if (worseScore == -1 || puntuacioEquip < worseScore) {   //Worse combination
            worseScore = puntuacioEquip;
            worseCombination = jugadors;
        }
    }//TODO -1 in C

    public void printStatistics(){
        threadMessenger.addMessageToQueue( Error.color_green + "*******THREAD " + Thread.currentThread().getId()+ " STATISTICS******"+
                "\nNúmero de Combinaciones evaluadas: " + numComb +
                "\nNúmero de combinaciones no válidas: " + numInvComb +
                "\nCoste promedio de las combinaciones válidas: " + avgCostValidComb +
                "\nPuntuación promedio de las combinaciones válidas: " + avgScoreValidComb +
                "\nMejor combinación (desde el punto de vista de la puntuación): " + bestCombination.toStringEquipJugadors() +
                "\nPeor combinación (desde el punto de vista de la puntuación): " + worseCombination.toStringEquipJugadors() +
                "\n********************************************************" + Error.end_color);
    }

    public void calculateGlobalStatistics(Statistics evaluatorStatistics){
        numComb += evaluatorStatistics.getNumComb();
        numInvComb += evaluatorStatistics.getNumInvComb();
        numValidComb += evaluatorStatistics.getNumValidComb();
        avgCostValidComb = ((avgCostValidComb * numValidComb) + (evaluatorStatistics.getAvgCostValidComb() * evaluatorStatistics.getNumValidComb())) / numValidComb;
        avgScoreValidComb = ((avgScoreValidComb * numValidComb) + (evaluatorStatistics.getAvgScoreValidComb() * evaluatorStatistics.getNumValidComb())) / numValidComb;
        if (evaluatorStatistics.getBestScore() > bestScore){    //Best combination regarding points
            bestScore = evaluatorStatistics.getBestScore();
            bestCombination = evaluatorStatistics.getBestCombination();
        }
        if (worseScore == -1|| evaluatorStatistics.getWorseScore() < worseScore) {   //Worse combination regarding points
            worseScore = evaluatorStatistics.getWorseScore();
            worseCombination = evaluatorStatistics.getWorseCombination();
        }
    }//TODO fix else if in C

    public void printGlobalStatistics(){
        threadMessenger.addMessageToQueue(Error.color_blue + "*******GLOBAL STATISTICS******"+
                "\nNúmero de Combinaciones evaluadas: " + numComb +
                "\nNúmero de combinaciones no válidas: " + numInvComb +
                "\nCoste promedio de las combinaciones válidas: " + avgCostValidComb +
                "\nPuntuación promedio de las combinaciones válidas: " + avgScoreValidComb +
                "\nMejor combinación (desde el punto de vista de la puntuación): " + bestCombination.toStringEquipJugadors() +
                "\nPeor combinación (desde el punto de vista de la puntuación): " + worseCombination.toStringEquipJugadors() +
                "\n********************************************************" + Error.end_color);
    }

    public int getNumComb() {
        return numComb;
    }

    public void setNumComb(int numComb) {
        this.numComb = numComb;
    }

    public int getNumInvComb() {
        return numInvComb;
    }

    public void setNumInvComb(int numInvComb) {
        this.numInvComb = numInvComb;
    }

    public int getNumValidComb() {
        return numValidComb;
    }

    public void setNumValidComb(int numValidComb) {
        this.numValidComb = numValidComb;
    }

    public int getAvgCostValidComb() {
        return avgCostValidComb;
    }

    public void setAvgCostValidComb(int avgCostValidComb) {
        this.avgCostValidComb = avgCostValidComb;
    }

    public int getAvgScoreValidComb() {
        return avgScoreValidComb;
    }

    public void setAvgScoreValidComb(int avgScoreValidComb) {
        this.avgScoreValidComb = avgScoreValidComb;
    }

    public JugadorsEquip getBestCombination() {
        return bestCombination;
    }

    public void setBestCombination(JugadorsEquip bestCombination) {
        this.bestCombination = bestCombination;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }

    public JugadorsEquip getWorseCombination() {
        return worseCombination;
    }

    public void setWorseCombination(JugadorsEquip worseCombination) {
        this.worseCombination = worseCombination;
    }

    public int getWorseScore() {
        return worseScore;
    }

    public void setWorseScore(int worseScore) {
        this.worseScore = worseScore;
    }

    public void incrementNumComb(){
        this.numComb++;
    }

    public void incrementNumInvComb(){
        this.numInvComb++;
    }

}
