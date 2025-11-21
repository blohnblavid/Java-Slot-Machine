package app;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Model {


    static class Symbol {
        final String name;
        final String imagePath;
        final int weight;
        final int payout3;

        Symbol(String name, String imagePath, int weight, int payout3) {
            this.name = name;
            this.imagePath = imagePath;
            this.weight = weight;
            this.payout3 = payout3;
        }
    }

    static class Reel {
        private final List<Symbol> bucket = new ArrayList<>();
        private final Random rng;

        Reel(List<Symbol> symbols, Random rng) {
            this.rng = rng;
            for (Symbol s : symbols) {
                for (int i = 0; i < s.weight; i++) {
                    bucket.add(s);
                }
            }
        }

        Symbol spin() {
            int idx = rng.nextInt(bucket.size());
            return bucket.get(idx);
        }
    }

    static class SlotRoll {
        private final Reel[] reels;

        SlotRoll(Reel... reels) {
            this.reels = reels;
        }

        int spinOnce(int bet, List<Symbol> outSymbols) {
            outSymbols.clear();
            for (Reel r : reels) outSymbols.add(r.spin());
            boolean threeKind = outSymbols.get(0).name.equals(outSymbols.get(1).name)
                    && outSymbols.get(1).name.equals(outSymbols.get(2).name);
            if (threeKind) {
                int mult = outSymbols.get(0).payout3;
                return bet * mult;
            }
            return 0;
        }
    }

    public static class Bank {
        public int credits;
        Bank(int start) { credits = start; }
        public boolean canBet(int bet) { return bet > 0 && credits >= bet; }
        public void applyBet(int bet) { credits -= bet; }
        public void applyWin(int win) { credits += win; }
    }

    public final List<Symbol> symbols = List.of(
            new Symbol("cherry","/images/cherry.png",40,5),
            new Symbol("lemon","/images/lemon.png",30,10),
            new Symbol("bell","/images/bell.png",20,20),
            new Symbol("diamond","/images/diamond.png",8,50),
            new Symbol("seven","/images/seven.png",2,100)
    );

    private final Random rng = new SecureRandom();
    private final Reel r1 = new Reel(symbols, rng);
    private final Reel r2 = new Reel(symbols, rng);
    private final Reel r3 = new Reel(symbols, rng);
    private final SlotRoll slot = new SlotRoll(r1, r2, r3);

    public final Bank bank = new Bank(200);
    public final List<Symbol> outcome = new ArrayList<>(3);

    private Runnable onBalanceChanged = () -> {};

    public void setOnBalanceChanged(Runnable r) { this.onBalanceChanged = (r != null ? r : () -> {}); }
    public int getBalance() { return bank.credits; }

    public SpinResult spin(int bet) {
        if (!bank.canBet(bet)) {
            return new SpinResult(false, 0, List.of());
        }
        bank.applyBet(bet);
        int win = slot.spinOnce(bet, outcome);
        if (win > 0) bank.applyWin(win);
        onBalanceChanged.run();
        return new SpinResult(true, win, List.copyOf(outcome));
    }

    public void adjustCredits(int delta) {
        bank.credits += delta;
        if (bank.credits < 0) bank.credits = 0;
        onBalanceChanged.run();
    }

    public void resetBalance(int to) {
        bank.credits = Math.max(0, to);
        onBalanceChanged.run();
    }

    public record SpinResult(boolean accepted, int win, List<Symbol> symbols) {}
}