package app;

public class Skin {
    public final String id;
    public final String name;
    public final String displayName;
    public final int price;
    public final String cssResource;

    public Skin(String id, String name, int price, String cssResource) {
        this.id = id;
        this.name = name;
        this.displayName = name;
        this.price = price;
        this.cssResource = cssResource;
    }
}