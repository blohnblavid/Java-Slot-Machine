package app;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.List;
import java.util.Random;

public class GameView {

    private final Model model;
    private final SkinManager skinManager;
    private final StatsManager stats;

    private final BorderPane root = new BorderPane();
    private final Label balance = new Label();
    private final Label status = new Label("Place your bet");

    private final ImageView r1 = reelImage();
    private final ImageView r2 = reelImage();
    private final ImageView r3 = reelImage();

    private final Spinner<Integer> betSpinner = new Spinner<>();
    private Button spinBtn;

    private final Image blankImage = new Image(getClass().getResourceAsStream("/images/blank.png"));
    private List<Image> symbolImages;
    private Timeline winBlink;

    public GameView(Model model, SkinManager skinManager, StatsManager stats) {
        this.model = model;
        this.skinManager = skinManager;
        this.stats = stats;
        build();

        symbolImages = model.symbols.stream()
                .map(s -> new Image(getClass().getResourceAsStream(s.imagePath)))
                .toList();

        refreshBalance();
        refreshSkin();
    }

    private ImageView reelImage() {
        ImageView iv = new ImageView(blankImage);
        iv.getStyleClass().add("reel");
        iv.setFitWidth(120);
        iv.setFitHeight(120);
        iv.setPreserveRatio(true);
        return iv;
    }

    private void build() {
        root.getStyleClass().add("game-root");

        VBox top = new VBox(6);
        top.setPadding(new Insets(16));
        top.setAlignment(Pos.CENTER);
        Label title = new Label("JAVA SLOTS");
        title.getStyleClass().add("title");

        balance.getStyleClass().add("balance");
        status.getStyleClass().add("status");
        top.getChildren().addAll(title, balance, status);

        HBox reels = new HBox(12, r1, r2, r3);
        reels.setAlignment(Pos.CENTER);
        reels.setPadding(new Insets(20));
        reels.getStyleClass().add("reels");

        HBox controls = new HBox(12);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(16));
        betSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 10));
        spinBtn = new Button("SPIN");
        Button reset = new Button("RESET");
        Button applySkin = new Button("Reapply Skin");

        spinBtn.setOnAction(e -> onSpin());
        reset.setOnAction(e -> {
            model.bank.credits = 200;
            status.setText("Balance reset.");
            refreshBalance();
        });
        applySkin.setOnAction(e -> refreshSkin());

        controls.getChildren().addAll(new Label("Bet:"), betSpinner, spinBtn, reset, applySkin);

        root.setTop(top);
        root.setCenter(reels);
        root.setBottom(controls);
    }

    private void animateReels(List<Model.Symbol> outcome, boolean isWin, Runnable after) {
        if (winBlink != null) { winBlink.stop(); r1.setEffect(null); r2.setEffect(null); r3.setEffect(null); }

        Random rand = new Random();

        Timeline t1 = new Timeline(new KeyFrame(Duration.millis(60),
                ev -> r1.setImage(symbolImages.get(rand.nextInt(symbolImages.size())))
        ));
        t1.setCycleCount(12);

        Timeline t2 = new Timeline(new KeyFrame(Duration.millis(60),
                ev -> r2.setImage(symbolImages.get(rand.nextInt(symbolImages.size())))
        ));
        t2.setCycleCount(14);
        t2.setDelay(Duration.millis(120));

        Timeline t3 = new Timeline(new KeyFrame(Duration.millis(60),
                ev -> r3.setImage(symbolImages.get(rand.nextInt(symbolImages.size())))
        ));
        t3.setCycleCount(16);
        t3.setDelay(Duration.millis(240));

        t3.setOnFinished(ev -> {
            r1.setImage(new Image(getClass().getResourceAsStream(outcome.get(0).imagePath)));
            r2.setImage(new Image(getClass().getResourceAsStream(outcome.get(1).imagePath)));
            r3.setImage(new Image(getClass().getResourceAsStream(outcome.get(2).imagePath)));
            if (isWin) playWinBlink();
            if (after != null) after.run();
        });

        t1.play();
        t2.play();
        t3.play();
    }

    private void playWinBlink() {
        DropShadow d1 = new DropShadow(0, Color.LAWNGREEN);
        DropShadow d2 = new DropShadow(0, Color.LAWNGREEN);
        DropShadow d3 = new DropShadow(0, Color.LAWNGREEN);
        r1.setEffect(d1); r2.setEffect(d2); r3.setEffect(d3);

        winBlink = new Timeline(
                new KeyFrame(Duration.millis(0),   e -> { d1.setRadius(0);  d2.setRadius(0);  d3.setRadius(0);  }),
                new KeyFrame(Duration.millis(120), e -> { d1.setRadius(25); d2.setRadius(25); d3.setRadius(25); }),
                new KeyFrame(Duration.millis(240), e -> { d1.setRadius(0);  d2.setRadius(0);  d3.setRadius(0);  })
        );
        winBlink.setCycleCount(8);
        winBlink.play();
    }


    private void onSpin() {
        int bet = betSpinner.getValue();
        if (!model.bank.canBet(bet)) {
            status.setText("Insufficient credits!");
            return;
        }
        Model.SpinResult res = model.spin(bet);
        if (!res.accepted()) {
            status.setText("Bet not accepted.");
            return;
        }

        spinBtn.setDisable(true);

        final int winAmt = res.win();
        animateReels(res.symbols(), winAmt > 0, () -> {
            if (stats != null) {
                stats.recordSpin(bet, winAmt);
            }
            if (winAmt > 0) {
                status.setText("WIN +" + winAmt + "!");
            } else {
                status.setText("No win. Try again!");
            }
            refreshBalance();
            spinBtn.setDisable(false);
        });
    }

    public void refreshBalance() {
        balance.setText("BALANCE: " + model.getBalance());
    }

    public void refreshSkin() {
        Node n = root.getScene() != null ? root.getScene().getRoot() : root;
        if (root.getScene() != null) {
            skinManager.apply(root.getScene());
        }
    }

    public BorderPane getRoot() { return root; }
}