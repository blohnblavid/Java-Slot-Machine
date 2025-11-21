import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SlotMachineGUI {

    private SlotMachineTier1.Bank bank;
    private SlotMachineTier1.SlotMachine slotMachine;
    private List<SlotMachineTier1.Symbol> outcome;

    private JFrame frame;
    private JLabel[] reelLabels;
    private JTextField betField;
    private JLabel balanceLabel;
    private JLabel resultLabel;
    private JButton spinButton;
    private JButton resetButton;

    public SlotMachineGUI(SlotMachineTier1.SlotMachine slotMachine, SlotMachineTier1.Bank bank) {
        this.slotMachine = slotMachine;
        this.bank = bank;
        this.outcome = new ArrayList<>(3);

        setupGUI();
    }

    private void setupGUI() {
        frame = new JFrame("Java Slot Machine");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 300);
        frame.setLayout(new BorderLayout());

        // --- Top panel: balance and result ---
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        balanceLabel = new JLabel("Balance: " + bank.credits);
        balanceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        resultLabel = new JLabel("Enter a bet and spin!");
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(balanceLabel);
        topPanel.add(resultLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        // --- Center panel: reels ---
        JPanel reelsPanel = new JPanel(new FlowLayout());
        reelLabels = new JLabel[3];
        for (int i = 0; i < 3; i++) {
            reelLabels[i] = new JLabel();
            reelLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            reelLabels[i].setPreferredSize(new Dimension(100, 100));
            // Set default blank image
            reelLabels[i].setIcon(new ImageIcon(getClass().getResource("/images/blank.png")));
            reelsPanel.add(reelLabels[i]);
        }
        frame.add(reelsPanel, BorderLayout.CENTER);

        // --- Bottom panel: controls ---
        JPanel controlsPanel = new JPanel(new FlowLayout());
        betField = new JTextField(5);
        spinButton = new JButton("Spin");
        resetButton = new JButton("Reset");
        controlsPanel.add(new JLabel("Bet:"));
        controlsPanel.add(betField);
        controlsPanel.add(spinButton);
        controlsPanel.add(resetButton);
        frame.add(controlsPanel, BorderLayout.SOUTH);

        // --- Button listeners ---
        spinButton.addActionListener(e -> spin());
        resetButton.addActionListener(e -> reset());

        // Focus on text field when window opens
        frame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                betField.requestFocusInWindow();
            }
        });

        frame.setVisible(true);
    }

    private void spin() {
        int bet;
        try {
            bet = Integer.parseInt(betField.getText());
        } catch (NumberFormatException e) {
            resultLabel.setText("Invalid bet.");
            return;
        }

        if (!bank.canBet(bet)) {
            resultLabel.setText("Insufficient credits.");
            return;
        }

        bank.applyBet(bet);
        int win = slotMachine.spinOnce(bet, outcome);

        // Update reels with symbol images
        for (int i = 0; i < outcome.size(); i++) {
            reelLabels[i].setIcon(outcome.get(i).icon);
        }

        // Update result
        if (win > 0) {
            bank.applyWin(win);
            resultLabel.setText("WIN! +" + win + " credits");
        } else {
            resultLabel.setText("No win, try again!");
        }

        balanceLabel.setText("Balance: " + bank.credits);
    }

    private void reset() {
        bank.reset(200);
        for (JLabel reel : reelLabels) {
            reel.setIcon(new ImageIcon(getClass().getResource("/images/blank.png")));
        }
        balanceLabel.setText("Balance: " + bank.credits);
        resultLabel.setText("Game reset!");
        betField.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create symbols with images
            List<SlotMachineTier1.Symbol> symbols = List.of(
                    new SlotMachineTier1.Symbol("cherry", "CH", 40, 5, "images/cherry (1).png"),
                    new SlotMachineTier1.Symbol("lemon", "LE", 30, 10, "images/lemon (1).png"),
                    new SlotMachineTier1.Symbol("bell", "BEL", 20, 20, "images/bell (1).png"),
                    new SlotMachineTier1.Symbol("diamond", "DIA", 8, 50, "images/diamond (1).png"),
                    new SlotMachineTier1.Symbol("seven", "777", 2, 100, "images/seven (1).png")
            );

            Random rng = new Random();
            SlotMachineTier1.Reel r1 = new SlotMachineTier1.Reel(symbols, rng);
            SlotMachineTier1.Reel r2 = new SlotMachineTier1.Reel(symbols, rng);
            SlotMachineTier1.Reel r3 = new SlotMachineTier1.Reel(symbols, rng);
            SlotMachineTier1.SlotMachine sm = new SlotMachineTier1.SlotMachine(r1, r2, r3);
            SlotMachineTier1.Bank bank = new SlotMachineTier1.Bank(200);

            new SlotMachineGUI(sm, bank);
        });
    }
}