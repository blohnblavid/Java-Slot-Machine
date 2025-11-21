package app;

public class StatsManager {
    private long totalSpins;
    private long totalWins;
    private long totalCreditsWon;
    private long totalCreditsLost;
    private long biggestWin;
    private long totalPurchases;
    private long creditsSpent;

    public void recordSpin(int bet, int win) {
        totalSpins++;
        if (win > 0) {
            totalWins++;
            totalCreditsWon += win;
            if (win > biggestWin) biggestWin = win;
        } else {
            totalCreditsLost += bet;
        }
    }

    public void recordPurchase(int price) {
        totalPurchases++;
        creditsSpent += price;
    }

    public void resetSession() {
        totalSpins = totalWins = totalCreditsWon = totalCreditsLost = biggestWin = totalPurchases = creditsSpent = 0;
    }

    public long getTotalSpins() { return totalSpins; }
    public long getTotalWins() { return totalWins; }
    public long getTotalCreditsWon() { return totalCreditsWon; }
    public long getTotalCreditsLost() { return totalCreditsLost; }
    public long getBiggestWin() { return biggestWin; }
    public long getTotalPurchases() { return totalPurchases; }
    public long getCreditsSpent() { return creditsSpent; }
}