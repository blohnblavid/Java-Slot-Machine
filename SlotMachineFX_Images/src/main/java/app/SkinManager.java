package app;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SkinManager {

    private final List<Skin> skins = new ArrayList<>();
    private final Set<String> owned = new HashSet<>();
    private Skin current;

    private Timeline rainbowAnim;
    private Timeline sweepAnim;
    private Rectangle sweepRect;

    public SkinManager() {
        skins.add(new Skin("classic", "Classic", 0, "/styles/classic.css"));
        skins.add(new Skin("emerald", "Emerald", 500, "/styles/emerald.css"));
        skins.add(new Skin("royal", "Royal Gold", 1500, "/styles/royal.css"));
        skins.add(new Skin("rainbow", "Rainbow Dream", 5000, "/styles/rainbow.css"));

        current = skins.get(0);
        owned.add(current.id);
    }

    public List<Skin> all() { return skins; }
    public boolean isOwned(String id) { return owned.contains(id); }
    public Skin current() { return current; }
    public void acquire(String id) { owned.add(id); }

    public boolean equip(String id) {
        for (Skin s : skins) {
            if (s.id.equals(id) && owned.contains(id)) {
                current = s;
                return true;
            }
        }
        return false;
    }

    public void apply(Scene scene) {

        scene.getStylesheets().clear();
        if (current == null) return;
        URL url = getClass().getResource(current.cssResource);
        if (url != null) scene.getStylesheets().add(url.toExternalForm());

        if (rainbowAnim != null) { rainbowAnim.stop(); rainbowAnim = null; }
        if (sweepAnim != null)   { sweepAnim.stop();   sweepAnim   = null; }
        if (scene.getRoot() != null) {
            scene.getRoot().setEffect(null);
        }
        if (sweepRect != null && scene.getRoot() instanceof StackPane spOld) {
            spOld.getChildren().remove(sweepRect);
            sweepRect = null;
        }

        if ("rainbow".equals(current.id)) {
            ColorAdjust hue = new ColorAdjust();
            hue.setHue(-1.0);
            scene.getRoot().setEffect(hue);

            rainbowAnim = new Timeline(
                    new KeyFrame(Duration.ZERO,       new KeyValue(hue.hueProperty(), -1.0)),
                    new KeyFrame(Duration.seconds(6), new KeyValue(hue.hueProperty(),  1.0))
            );
            rainbowAnim.setAutoReverse(true);
            rainbowAnim.setCycleCount(Animation.INDEFINITE);
            rainbowAnim.play();
        }

        if (scene.getRoot() instanceof StackPane sp) {
            sweepRect = new Rectangle();
            sweepRect.setManaged(false);
            sweepRect.setMouseTransparent(true);

            sweepRect.setFill(new LinearGradient(
                    0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0.00, Color.TRANSPARENT),
                    new Stop(0.45, Color.rgb(255,255,255,0.10)),
                    new Stop(0.55, Color.rgb(255,255,255,0.18)),
                    new Stop(1.00, Color.TRANSPARENT)
            ));

            sp.widthProperty().addListener((obs, oldV, w) -> sweepRect.setWidth(w.doubleValue()));
            sp.heightProperty().addListener((obs, oldV, h) -> sweepRect.setHeight(h.doubleValue()));

            sweepRect.setRotate(12);
            sweepRect.setTranslateX(-800);

            sp.getChildren().add(0, sweepRect);

            sweepAnim = new Timeline(
                    new KeyFrame(Duration.ZERO,         new KeyValue(sweepRect.translateXProperty(), -800)),
                    new KeyFrame(Duration.seconds(3.5), new KeyValue(sweepRect.translateXProperty(),  800))
            );
            sweepAnim.setCycleCount(Animation.INDEFINITE);
            sweepAnim.setAutoReverse(true);
            sweepAnim.play();
        }
    }
}