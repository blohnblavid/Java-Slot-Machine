package app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ShopView {

    private final Model model;
    private final SkinManager skinManager;
    private final StatsManager stats;
    private final Shop shop;

    private final BorderPane root = new BorderPane();
    private final Label balance = new Label();
    private final VBox list = new VBox(10);

    private Runnable onSkinEquipped = () -> {};

    public ShopView(Model model, SkinManager skinManager, StatsManager stats) {
        this.model = model;
        this.skinManager = skinManager;
        this.stats = stats;
        this.shop = new Shop(model, skinManager);
        build();
        refreshBalance();
        refreshList();
    }

    private void build() {
        root.getStyleClass().add("shop-root");

        VBox top = new VBox(6);
        top.setPadding(new Insets(16));
        top.setAlignment(Pos.CENTER);
        Label title = new Label("SHOP");
        title.getStyleClass().add("title");
        balance.getStyleClass().add("balance");
        top.getChildren().addAll(title, balance);

        list.setPadding(new Insets(16));
        root.setTop(top);
        root.setCenter(list);
    }

    private Node skinRow(Skin s) {
        HBox row = new HBox(12);
        row.getStyleClass().add("card");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));

        Label name = new Label(s.displayName);
        name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label price = new Label(s.price == 0 ? "FREE" : (s.price + " cr"));
        price.setStyle("-fx-opacity: 0.85;");

        Label status = new Label();
        Button action = new Button();

        boolean owned = skinManager.isOwned(s.id);
        boolean equipped = skinManager.current() != null && skinManager.current().id.equals(s.id);

        if (equipped) {
            status.setText("Equipped");
            action.setText("Equipped");
            action.setDisable(true);
        } else if (owned) {
            status.setText("Owned");
            action.setText("Equip");
            action.setOnAction(e -> {
                if (skinManager.equip(s.id)) {
                    onSkinEquipped.run();
                    refreshList();
                }
            });
        } else {
            status.setText("Locked");
            action.setText("Buy");
            action.setOnAction(e -> {
                if (model.getBalance() >= s.price) {
                    if (shop.buy(s)) {
                        if (stats != null) stats.recordPurchase(s.price);
                        refreshBalance();
                        refreshList();
                    }
                } else {
                    action.setDisable(true);
                    action.setText("Need " + (s.price - model.getBalance()) + " more");
                }
            });
        }

        HBox spacer = new HBox();
        spacer.setMinWidth(0);
        spacer.setPrefWidth(20);
        spacer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        row.getChildren().addAll(name, price, spacer, status, action);
        return row;
    }

    private void refreshList() {
        list.getChildren().clear();
        for (Skin s : skinManager.all()) {
            list.getChildren().add(skinRow(s));
        }
    }

    public void refreshBalance() {
        balance.setText("BALANCE: " + model.getBalance());
    }

    public void setOnSkinEquipped(Runnable r) {
        this.onSkinEquipped = (r != null ? r : () -> {});
    }

    public BorderPane getRoot() {
        return root;
    }
}