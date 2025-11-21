package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    private final Model model = new Model();
    private final SkinManager skinManager = new SkinManager();
    private final StatsManager stats = new StatsManager();

    @Override
    public void start(Stage stage) {
        try { Font.loadFont(getClass().getResourceAsStream("/fonts/SHOWG.TTF"), 24); } catch (Exception ignored) {}

        BorderPane appRoot = new BorderPane();
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        GameView game = new GameView(model, skinManager, stats);
        ShopView shop = new ShopView(model, skinManager, stats);

        Tab gameTab = new Tab("Game", game.getRoot());
        Tab shopTab = new Tab("Shop", shop.getRoot());
        tabs.getTabs().addAll(gameTab, shopTab);
        appRoot.setCenter(tabs);

        StackPane container = new StackPane(appRoot);
        Scene scene = new Scene(container, 900, 600);
        skinManager.apply(scene);
        stage.setTitle("Java Slots — JavaFX + Shop");
        stage.setScene(scene);

        stage.setOnCloseRequest(ev -> {
            ev.consume();
            new StatsView(stats).showAndExit(stage);
        });

        stage.show();

        model.setOnBalanceChanged(() -> {
            game.refreshBalance();
            shop.refreshBalance();
        });
        shop.setOnSkinEquipped(game::refreshSkin);
    }

    public static void main(String[] args) { launch(); }
}