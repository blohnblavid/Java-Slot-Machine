import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SlotMachineGUI {

    private SlotMachine.Bank bank;
    private SlotMachine.SlotRoll slotMachine;
    private List<SlotMachine.Symbol> outcome;

    private JFrame frame;
    private JLabel[] reelLabels;
    private JTextField betField;
    private JLabel balanceLabel;
    private JLabel resultLabel;
    private JButton spinButton;
    private JButton resetButton;
    private JButton quitButton;

    // session stats
    private int spins = 0, wins = 0, totalBet = 0, creditsWon = 0, creditsLost = 0;

    // icons
    private final Icon[] ANIM_ICONS = {
            loadIcon("/resources/images/cherry.png"),
            loadIcon("/resources/images/lemon.png"),
            loadIcon("/resources/images/bell.png"),
            loadIcon("/resources/images/diamond.png"),
            loadIcon("/resources/images/seven.png")
    };
    private final Icon BLANK_ICON = loadIcon("/resources/images/blank.png");

    public SlotMachineGUI(SlotMachine.SlotRoll slotMachine, SlotMachine.Bank bank) {
        this.slotMachine = slotMachine;
        this.bank = bank;
        this.outcome = new ArrayList<>(3);
        setupGUI();
    }

    private static Icon loadIcon(String path) {
        java.net.URL url = SlotMachineGUI.class.getResource(path);
        return (url != null) ? new ImageIcon(url) : new ImageIcon();
    }

    private static class GradientPanel extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, new Color(20,10,25),
                    0, h, new Color(60,0,70)));
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }
    }

    private void setupGUI() {
        frame = new JFrame("Lucky Slots!");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel root = new GradientPanel();
        root.setLayout(new BorderLayout());
        frame.setContentPane(root);

        // load the fancy font or fallback
        Font casinoFont;
        try {
            casinoFont = new Font("Showcard Gothic", Font.BOLD, 22);
        } catch (Exception e) {
            casinoFont = new Font("Arial", Font.BOLD, 22);
        }

        // top
        JPanel topPanel = new JPanel(new GridLayout(2, 1)) {{ setOpaque(false); }};
        balanceLabel = new JLabel("BALANCE: " + bank.credits, SwingConstants.CENTER);
        balanceLabel.setFont(casinoFont.deriveFont(24f));
        balanceLabel.setForeground(new Color(255, 215, 0));
        resultLabel = new JLabel("PLACE YOUR BET!", SwingConstants.CENTER);
        resultLabel.setFont(casinoFont.deriveFont(Font.PLAIN, 18f));
        resultLabel.setForeground(new Color(255, 230, 150));
        topPanel.add(balanceLabel);
        topPanel.add(resultLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        // center reels
        JPanel reelsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 16)) {{ setOpaque(false); }};
        reelLabels = new JLabel[3];
        for (int i = 0; i < 3; i++) {
            reelLabels[i] = new JLabel(BLANK_ICON);
            reelLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            reelLabels[i].setPreferredSize(new Dimension(100, 100));
            reelsPanel.add(reelLabels[i]);
        }
        frame.add(reelsPanel, BorderLayout.CENTER);

        // bottom controls
        JPanel controlsPanel = new JPanel(new FlowLayout()) {{ setOpaque(false); }};
        JLabel betLbl = new JLabel("BET:");
        betLbl.setForeground(new Color(255, 215, 0));
        betLbl.setFont(casinoFont.deriveFont(18f));
        betField = new JTextField(6);
        spinButton = new JButton("SPIN");
        resetButton = new JButton("RESET");
        quitButton  = new JButton("QUIT");

        for (JButton b : new JButton[]{spinButton, resetButton, quitButton}) {
            b.setFont(casinoFont.deriveFont(16f));
            b.setForeground(Color.WHITE);
            b.setBackground(new Color(90, 0, 120));
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createLineBorder(new Color(255,215,0), 2));
            b.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e){ b.setBackground(new Color(130, 0, 170)); }
                public void mouseExited (MouseEvent e){ b.setBackground(new Color(90, 0, 120)); }
            });
        }

        controlsPanel.add(betLbl);
        controlsPanel.add(betField);
        controlsPanel.add(spinButton);
        controlsPanel.add(resetButton);
        controlsPanel.add(quitButton);
        frame.add(controlsPanel, BorderLayout.SOUTH);

        // actions
        spinButton.addActionListener(e -> spin());
        resetButton.addActionListener(e -> reset());
        quitButton.addActionListener(e -> { showStatsDialog(); frame.dispose(); });
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { showStatsDialog(); frame.dispose(); }
        });

        frame.setVisible(true);
    }

    private void spin() {
        int bet;
        try {
            bet = Integer.parseInt(betField.getText().trim());
        } catch (NumberFormatException e) {
            resultLabel.setText("INVALID BET!");
            return;
        }
        if (!bank.canBet(bet)) {
            resultLabel.setText("INSUFFICIENT CREDITS!");
            return;
        }

        totalBet += bet;
        bank.applyBet(bet);
        balanceLabel.setText("BALANCE: " + bank.credits);
        int win = slotMachine.spinOnce(bet, outcome);
        spinButton.setEnabled(false);
        resultLabel.setText("SPINNING...");
        spins++;

        animateReel(0, 900, () -> reelLabels[0].setIcon(outcome.get(0).icon));
        animateReel(1, 1050, () -> reelLabels[1].setIcon(outcome.get(1).icon));
        animateReel(2, 1200, () -> {
            reelLabels[2].setIcon(outcome.get(2).icon);
            settleResult(win, bet);
            spinButton.setEnabled(true);
        });
    }

    private void animateReel(int index, int totalMs, Runnable onStop) {
        final long start = System.currentTimeMillis();
        final int[] pos = { (int)(Math.random() * ANIM_ICONS.length) };
        final Timer t = new Timer(60, null);
        t.addActionListener(ev -> {
            long elapsed = System.currentTimeMillis() - start;
            pos[0] = (pos[0] + 1) % ANIM_ICONS.length;
            reelLabels[index].setIcon(ANIM_ICONS[pos[0]]);
            double p = Math.min(1.0, elapsed / (double) totalMs);
            int base = 50, extra = 220;
            t.setDelay(base + (int)(extra * p * p));
            if (elapsed >= totalMs) { t.stop(); onStop.run(); }
        });
        t.start();
    }

    private void settleResult(int win, int betPlaced) {
        clearReelBorders();
        if (win > 0) {
            wins++;
            bank.applyWin(win);
            creditsWon += win;
            resultLabel.setForeground(new Color(255, 215, 0));
            resultLabel.setText("WIN! +" + win + " CREDITS!");
            flashWinners();
        } else {
            creditsLost += betPlaced;
            resultLabel.setForeground(new Color(255, 100, 100));
            resultLabel.setText("NO WIN – TRY AGAIN!");
        }
        balanceLabel.setText("BALANCE: " + bank.credits);
    }

    private void flashWinners() {
        Icon a = reelLabels[0].getIcon();
        Icon b = reelLabels[1].getIcon();
        Icon c = reelLabels[2].getIcon();
        if (a.equals(b) && b.equals(c)) flashAll();
        else if (a.equals(b)) flash(reelLabels[0], reelLabels[1]);
        else if (a.equals(c)) flash(reelLabels[0], reelLabels[2]);
        else if (b.equals(c)) flash(reelLabels[1], reelLabels[2]);
    }

    private void flash(JLabel... labels) {
        final Color on = new Color(255, 215, 0);
        Timer t = new Timer(120, null);
        final int[] count = {0};
        t.addActionListener(e -> {
            for (JLabel l : labels)
                l.setBorder((count[0] % 2 == 0) ? new LineBorder(on, 3, true) : null);
            count[0]++;
            if (count[0] > 10) {
                for (JLabel l : labels)
                    l.setBorder(new LineBorder(on, 3, true));
                ((Timer)e.getSource()).stop();
            }
        });
        t.start();
    }

    private void flashAll() { flash(reelLabels[0], reelLabels[1], reelLabels[2]); }
    private void clearReelBorders() { for (JLabel r : reelLabels) r.setBorder(null); }

    private void showStatsDialog() {
        int net = creditsWon - creditsLost;
        double winRate = (spins == 0) ? 0.0 : (wins * 100.0 / spins);
        String msg = "🎰 SESSION STATS 🎰\n\n" +
                "Spins: " + spins + "\n" +
                "Wins: " + wins + "\n" +
                String.format("Win Rate: %.1f%%\n", winRate) +
                "Total Wagered: " + totalBet + "\n" +
                "Credits Won: " + creditsWon + "\n" +
                "Credits Lost: " + creditsLost + "\n" +
                "Net: " + ((net >= 0) ? "+" : "") + net + "\n" +
                "\nFinal Balance: " + bank.credits;
        JOptionPane.showMessageDialog(frame, msg, "Session Stats", JOptionPane.INFORMATION_MESSAGE);
    }

    private void reset() {
        bank.reset(200);
        for (JLabel reel : reelLabels) reel.setIcon(BLANK_ICON);
        balanceLabel.setText("BALANCE: " + bank.credits);
        resultLabel.setForeground(new Color(255, 230, 150));
        resultLabel.setText("GAME RESET!");
        betField.requestFocusInWindow();
        spins = wins = totalBet = creditsWon = creditsLost = 0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            List<SlotMachine.Symbol> symbols = List.of(
                    new SlotMachine.Symbol("cherry", "CH", 40, 5, "/resources/images/cherry.png"),
                    new SlotMachine.Symbol("lemon", "LE", 30, 10, "/resources/images/lemon.png"),
                    new SlotMachine.Symbol("bell", "BEL", 20, 20, "/resources/images/bell.png"),
                    new SlotMachine.Symbol("diamond", "DIA", 8, 50, "/resources/images/diamond.png"),
                    new SlotMachine.Symbol("seven", "777", 2, 100, "/resources/images/seven.png")
            );

            Random rng = new Random();
            SlotMachine.Reel r1 = new SlotMachine.Reel(symbols, rng);
            SlotMachine.Reel r2 = new SlotMachine.Reel(symbols, rng);
            SlotMachine.Reel r3 = new SlotMachine.Reel(symbols, rng);
            SlotMachine.SlotRoll sm = new SlotMachine.SlotRoll(r1, r2, r3);
            SlotMachine.Bank bank = new SlotMachine.Bank(200);

            new SlotMachineGUI(sm, bank);
        });
    }
}
