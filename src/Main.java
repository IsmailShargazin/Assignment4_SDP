import java.util.List;

public class Main {

    public static void main(String[] args) {
        OrderFacade facade = new OrderFacade();

        List<Product> products = facade.getProducts();

        Product keyboard = products.get(0);
        Product mouse = products.get(1);

        Customer alice = new Customer(
                "Alice",
                "alice@example.com",
                "Astana, Abay 10"
        );

        Customer bob = new Customer(
                "Bob",
                "bob@example.com",
                "Astana, Abay 20"
        );

        System.out.println("=== 1. Successful order ===");

        Cart cart = new Cart();
        cart.add(keyboard, 1);
        cart.add(mouse, 2);

        OrderReceipt receipt = facade.placeOrder(cart, alice);

        System.out.println("Result: " + receipt);

        System.out.println(
                "\n=== 2. Payment declined and stock released ==="
        );

        Cart unpaidCart = new Cart();
        unpaidCart.add(mouse, 1);

        try {
            facade.placeOrder(unpaidCart, bob);

        } catch (OrderException exception) {
            System.out.println(
                    "Expected error: " + exception.getMessage()
            );
        }

        System.out.println(
                "\n=== 3. Insufficient stock ==="
        );

        Cart oversizedCart = new Cart();
        oversizedCart.add(keyboard, 99);

        try {
            facade.placeOrder(oversizedCart, alice);

        } catch (OrderException exception) {
            System.out.println(
                    "Expected error: " + exception.getMessage()
            );
        }

        System.out.println(
                "\n=== 4. Cancel the successful order ==="
        );

        System.out.println(
                "Result: " + facade.cancelOrder(receipt.orderId())
        );

        System.out.println(
                "\n=== 5. Prevent repeated cancellation ==="
        );

        try {
            facade.cancelOrder(receipt.orderId());

        } catch (OrderException exception) {
            System.out.println(
                    "Expected error: " + exception.getMessage()
            );
        }
    }
}