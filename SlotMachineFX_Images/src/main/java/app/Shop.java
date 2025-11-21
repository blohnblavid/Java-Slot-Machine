package app;

public class Shop {

    private final Model model;
    private final SkinManager skinManager;

    public Shop(Model model, SkinManager skinManager) {
        this.model = model;
        this.skinManager = skinManager;
    }

    public boolean buy(Skin skin) {
        if (skinManager.isOwned(skin.id)) return true;
        int bal = model.getBalance();
        if (bal >= skin.price) {
            model.adjustCredits(-skin.price);
            skinManager.acquire(skin.id);
            return true;
        }
        return false;
    }
}