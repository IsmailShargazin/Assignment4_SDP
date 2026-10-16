import java.util.Objects;

public class Product {

    private final String id;
    private final String name;
    private final long priceTenge;

    public Product(String id, String name, long priceTenge) {
        if (id == null || id.isBlank()
                || name == null || name.isBlank()) {

            throw new IllegalArgumentException(
                    "Product ID and name are required."
            );
        }

        if (priceTenge <= 0) {
            throw new IllegalArgumentException(
                    "Price must be positive."
            );
        }

        this.id = id;
        this.name = name;
        this.priceTenge = priceTenge;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public long priceTenge() {
        return priceTenge;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Product)) {
            return false;
        }

        Product other = (Product) object;

        return id.equals(other.id)
                && name.equals(other.name)
                && priceTenge == other.priceTenge;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, priceTenge);
    }
}