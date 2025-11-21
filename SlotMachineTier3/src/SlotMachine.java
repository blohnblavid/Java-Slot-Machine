import java.security.SecureRandom;
import java.util.*;
import javax.swing.*;

public class SlotMachine {

    static class Symbol {
        final String name;
        final String display;
        final int weight;
        final int payout3;
        final ImageIcon icon; // NEW FIELD for GUI

        // Constructor for GUI use (with image)
        Symbol(String name, String display, int weight, int payout3, String iconPath) {
            this.name = name;
            this.display = display;
            this.weight = weight;
            this.payout3 = payout3;
            if (iconPath != null) {
                    java.net.URL url = getClass().getResource(iconPath);
                    if (url != null) {
                        this.icon = new ImageIcon(url);
                    } else {
                        // fallback to a blank image in resources
                        java.net.URL blank = getClass().getResource("/resources/images/blank.png");
                        this.icon = blank != null ? new ImageIcon(blank) : new ImageIcon();
                    }
                } else {
                    this.icon = null;
                }
        }

        // Original constructor for console-only use
        Symbol(String name, String display, int weight, int payout3) {
            this(name, display, weight, payout3, null);
        }

        @Override
        public String toString() {
            return display;
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
            return bucket.get(rng.nextInt(bucket.size()));
        }
    }

    static class Bank {
        int credits;

        Bank(int start) {
            credits = start;
        }

        boolean canBet(int bet) {
            return bet > 0 && credits >= bet;
        }

        void applyBet(int bet) {
            credits -= bet;
        }

        void applyWin(int win) {
            credits += win;
        }

        void reset(int start) {
            credits = start;
        }
    }

    static class SlotRoll {
        private final Reel[] reels;

        SlotRoll(Reel... reels) {
            this.reels = reels;
        }

        int spinOnce(int bet, List<Symbol> outSymbols) {
            outSymbols.clear();
            for (Reel r : reels) {
                outSymbols.add(r.spin());
            }
            boolean threeKind = outSymbols.get(0).name.equals(outSymbols.get(1).name)
                    && outSymbols.get(1).name.equals(outSymbols.get(2).name);
            if (threeKind) {
                int mult = outSymbols.get(0).payout3;
                return bet * mult;
            }
            return 0;
        }
    }

    public static void main(String[] args) {
        List<Symbol> symbols = List.of(
                new Symbol("cherry", "CH", 40, 5),
                new Symbol("lemon", "LE", 30, 10),
                new Symbol("bell", "BEL", 20, 20),
                new Symbol("diamond", "DIA", 8, 50),
                new Symbol("seven", "777", 2, 100)
        );

        Random rng = new SecureRandom();
        Reel r1 = new Reel(symbols, rng);
        Reel r2 = new Reel(symbols, rng);
        Reel r3 = new Reel(symbols, rng);
        SlotRoll sm = new SlotRoll(r1, r2, r3);

        Bank bank = new Bank(200);
        List<Symbol> outcome = new ArrayList<>(3);

        // Stats
        int spins = 0;
        int wins = 0;
        int losses = 0;
        int biggestWin = 0;
        int resets = 0;

        System.out.println("=== Java Slots (Tier 1 Console) ===");
        System.out.println("Type a number to bet, 0 to quit, or 'reset' to start over.");
        System.out.println("Paytable (3 in a row): CH=5x, LE=10x, BEL=20x, DIA=50x, 777=100x");

        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.print("\nEnter bet (1-50), 'reset', or 0 to quit: ");
                String input = sc.next();

                if (input.equalsIgnoreCase("reset")) {
                    bank.reset(200);
                    spins = 0;
                    wins = 0;
                    losses = 0;
                    biggestWin = 0;
                    resets++;
                    System.out.println("\n--- Game Reset! Balance restored to 200 credits. ---");
                    continue;
                }

                int bet;
                try {
                    bet = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Enter a number, 'reset', or 0.");
                    continue;
                }

                if (bet == 0) break;
                if (bet < 1 || bet > 50) {
                    System.out.println("Bad bet. Try 1-50.");
                    continue;
                }
                if (!bank.canBet(bet)) {
                    System.out.println("Insufficient credits.");
                    continue;
                }

                spins++;
                bank.applyBet(bet);
                int win = sm.spinOnce(bet, outcome);
                System.out.printf("Reels: %s | %s | %s  -> ", outcome.get(0), outcome.get(1), outcome.get(2));
                if (win > 0) {
                    System.out.println("WIN! +" + win + " credits");
                    bank.applyWin(win);
                    wins++;
                    if (win > biggestWin) biggestWin = win;
                } else {
                    System.out.println("no win :(.");
                    losses++;
                }
                System.out.println("Balance: " + bank.credits);
            }
        }
    }
}