import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Cart {

    private final Map<Product, Integer> items = new LinkedHashMap<>();

    public void add(Product product, int quantity) {
        Objects.requireNonNull(product, "Product is required.");

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be positive."
            );
        }

        int combinedQuantity = Math.addExact(
                items.getOrDefault(product, 0),
                quantity
        );

        items.put(product, combinedQuantity);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Map<Product, Integer> getItems() {
        return Collections.unmodifiableMap(items);
    }

    public long totalTenge() {
        long total = 0;

        for (Map.Entry<Product, Integer> item : items.entrySet()) {
            Product product = item.getKey();
            int quantity = item.getValue();

            long lineTotal = Math.multiplyExact(
                    product.priceTenge(),
                    quantity
            );

            total = Math.addExact(total, lineTotal);
        }

        return total;
    }

    public Cart copy() {
        Cart copy = new Cart();
        copy.items.putAll(items);

        return copy;
    }
}