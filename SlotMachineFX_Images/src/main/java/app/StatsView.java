package app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class StatsView {
    private final StatsManager stats;

    public StatsView(StatsManager stats) { this.stats = stats; }

    public void showAndExit(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Session Stats");

        Label title = new Label("SESSION STATS");
        title.getStyleClass().add("title");

        Label l1 = new Label("Total Spins: " + stats.getTotalSpins());
        Label l2 = new Label("Total Wins: " + stats.getTotalWins());
        Label l3 = new Label("Biggest Win: " + stats.getBiggestWin());
        Label l4 = new Label("Credits Won: " + stats.getTotalCreditsWon());
        Label l5 = new Label("Credits Lost: " + stats.getTotalCreditsLost());
        Label l6 = new Label("Shop Purchases: " + stats.getTotalPurchases());
        Label l7 = new Label("Credits Spent in Shop: " + stats.getCreditsSpent());

        Button close = new Button("Save & Exit");
        close.setOnAction(e -> {
            dialog.close();
            owner.close();
        });

        VBox box = new VBox(10, title, l1, l2, l3, l4, l5, l6, l7, close);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        Scene scene = new Scene(box, 360, 360);
        dialog.setScene(scene);
        dialog.show();
    }
}