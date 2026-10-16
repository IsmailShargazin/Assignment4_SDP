import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InventoryService {

    private final Map<Product, Integer> stock = new LinkedHashMap<>();

    private final Map<String, Cart> reservations = new LinkedHashMap<>();

    public InventoryService(Map<Product, Integer> initialStock) {
        for (Map.Entry<Product, Integer> item : initialStock.entrySet()) {
            Product product = item.getKey();
            int quantity = item.getValue();

            if (quantity < 0 || containsProductId(product.id())) {
                throw new IllegalArgumentException(
                        "Invalid stock or duplicate product ID."
                );
            }

            stock.put(product, quantity);
        }
    }

    private boolean containsProductId(String productId) {
        for (Product product : stock.keySet()) {
            if (product.id().equals(productId)) {
                return true;
            }
        }

        return false;
    }

    public List<Product> getProducts() {
        return List.copyOf(stock.keySet());
    }

    public int available(Product product) {
        return stock.getOrDefault(product, 0);
    }

    public void reserve(String orderId, Cart cart) {
        if (cart.isEmpty() || reservations.containsKey(orderId)) {
            throw new OrderException(
                    "Empty cart or existing reservation."
            );
        }

        // Check every item before changing stock.
        for (Map.Entry<Product, Integer> item : cart.getItems().entrySet()) {
            Product product = item.getKey();
            int quantity = item.getValue();

            if (!stock.containsKey(product)
                    || available(product) < quantity) {

                throw new OrderException(
                        "Not enough stock: " + product.name()
                );
            }
        }

        // Reserve stock only after the entire cart is valid.
        for (Map.Entry<Product, Integer> item : cart.getItems().entrySet()) {
            Product product = item.getKey();
            int quantity = item.getValue();

            stock.put(
                    product,
                    available(product) - quantity
            );
        }

        reservations.put(orderId, cart.copy());

        System.out.println(
                "Inventory: stock reserved for " + orderId
        );
    }

    public void release(String orderId) {
        Cart reservation = reservations.remove(orderId);

        if (reservation == null) {
            return;
        }

        for (Map.Entry<Product, Integer> item
                : reservation.getItems().entrySet()) {

            Product product = item.getKey();
            int quantity = item.getValue();

            stock.put(
                    product,
                    available(product) + quantity
            );
        }

        System.out.println(
                "Inventory: stock released for " + orderId
        );
    }
}