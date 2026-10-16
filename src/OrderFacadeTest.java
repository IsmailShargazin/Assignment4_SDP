import java.util.LinkedHashMap;
import java.util.Map;

public class OrderFacadeTest {

    private final Product keyboard =
            new Product("P1", "Keyboard", 10_000);

    private final Product mouse =
            new Product("P2", "Mouse", 5_000);

    private final Customer alice =
            new Customer("Alice", "alice@example.com", "Astana");

    private final Customer bob =
            new Customer("Bob", "bob@example.com", "Astana");

    private final InventoryService inventory;

    private final PaymentProcessor payments =
            new PaymentProcessor(
                    Map.of(
                            "alice@example.com", 30_000L,
                            "bob@example.com", 1_000L
                    )
            );

    private final ShippingService shipping;

    private final NotificationService notifications =
            new NotificationService();

    private final OrderFacade facade;

    public OrderFacadeTest(ShippingService shipping) {
        Map<Product, Integer> stock = new LinkedHashMap<>();

        stock.put(keyboard, 5);
        stock.put(mouse, 3);

        inventory = new InventoryService(stock);

        this.shipping = shipping;

        facade = new OrderFacade(
                inventory,
                payments,
                shipping,
                notifications
        );
    }

    public static void main(String[] args) {
        new OrderFacadeTest(new ShippingService())
                .successAndCancellation();

        new OrderFacadeTest(new ShippingService())
                .declinedPaymentRestoresStock();

        new OrderFacadeTest(new ShippingService())
                .stockFailureIsAtomic();

        new OrderFacadeTest(new ShippingService())
                .invalidOrdersHaveNoSideEffects();

        ShippingService failingShipping = new ShippingService() {
            @Override
            public String create(String orderId, Customer customer) {
                super.create(orderId, customer);

                throw new OrderException(
                        "Simulated shipping failure."
                );
            }
        };

        new OrderFacadeTest(failingShipping)
                .shippingFailureRefundsPayment();

        System.out.println(
                "\nALL 5 TEST SCENARIOS PASSED"
        );
    }

    private void successAndCancellation() {
        Cart cart = new Cart();

        cart.add(keyboard, 1);
        cart.add(mouse, 2);

        OrderReceipt receipt = facade.placeOrder(cart, alice);

        check(
                receipt.totalTenge() == 20_000,
                "Incorrect total"
        );

        check(
                receipt.status() == OrderReceipt.Status.PLACED,
                "Incorrect placed status"
        );

        check(
                inventory.available(keyboard) == 4
                        && inventory.available(mouse) == 1,
                "Stock was not reserved"
        );

        check(
                payments.balance(alice.email()) == 10_000,
                "Payment was not charged"
        );

        check(
                shipping.hasShipment(receipt.orderId()),
                "Missing shipment"
        );

        check(
                notifications.getMessages().size() == 1,
                "Missing confirmation"
        );

        // Editing the original cart must not change the reservation.
        cart.add(keyboard, 2);

        OrderReceipt cancelled =
                facade.cancelOrder(receipt.orderId());

        check(
                cancelled.status() == OrderReceipt.Status.CANCELLED,
                "Incorrect cancelled status"
        );

        check(
                inventory.available(keyboard) == 5
                        && inventory.available(mouse) == 3,
                "Incorrect stock restoration after cart edit"
        );

        check(
                payments.balance(alice.email()) == 30_000,
                "Refund missing"
        );

        check(
                !shipping.hasShipment(receipt.orderId()),
                "Shipment not cancelled"
        );

        expectOrderError(
                () -> facade.cancelOrder(receipt.orderId())
        );

        check(
                payments.balance(alice.email()) == 30_000,
                "Duplicate refund occurred"
        );

        check(
                inventory.available(keyboard) == 5,
                "Stock was restored twice"
        );
    }

    private void declinedPaymentRestoresStock() {
        Cart cart = new Cart();

        cart.add(mouse, 1);

        expectOrderError(
                () -> facade.placeOrder(cart, bob)
        );

        check(
                inventory.available(mouse) == 3,
                "Reservation leaked after declined payment"
        );

        check(
                payments.balance(bob.email()) == 1_000,
                "Declined payment changed balance"
        );

        check(
                !shipping.hasShipment("ORD-1"),
                "Failed order has a shipment"
        );

        check(
                notifications.getMessages().get(0).contains("failed"),
                "Missing failure notification"
        );
    }

    private void stockFailureIsAtomic() {
        Cart cart = new Cart();

        cart.add(keyboard, 1);
        cart.add(mouse, 99);

        expectOrderError(
                () -> facade.placeOrder(cart, alice)
        );

        check(
                inventory.available(keyboard) == 5
                        && inventory.available(mouse) == 3,
                "Partial stock update occurred"
        );

        check(
                payments.balance(alice.email()) == 30_000,
                "Stock failure charged payment"
        );
    }

    private void invalidOrdersHaveNoSideEffects() {
        expectOrderError(
                () -> facade.placeOrder(new Cart(), alice)
        );

        expectOrderError(
                () -> facade.cancelOrder("missing-order")
        );

        Cart forgedPrice = new Cart();

        forgedPrice.add(
                new Product("P1", "Keyboard", 1),
                1
        );

        expectOrderError(
                () -> facade.placeOrder(forgedPrice, alice)
        );

        try {
            new Cart().add(mouse, 0);

            throw new AssertionError(
                    "Zero quantity was accepted"
            );

        } catch (IllegalArgumentException expected) {
            check(
                    inventory.available(mouse) == 3,
                    "Invalid quantity changed stock"
            );
        }

        check(
                inventory.available(keyboard) == 5,
                "Invalid order changed stock"
        );

        check(
                payments.balance(alice.email()) == 30_000,
                "Invalid order changed balance"
        );
    }

    private void shippingFailureRefundsPayment() {
        Cart cart = new Cart();

        cart.add(keyboard, 1);

        expectOrderError(
                () -> facade.placeOrder(cart, alice)
        );

        check(
                inventory.available(keyboard) == 5,
                "Shipping failure leaked stock"
        );

        check(
                payments.balance(alice.email()) == 30_000,
                "Shipping failure was not refunded"
        );

        check(
                !shipping.hasShipment("ORD-1"),
                "Partial shipment was not removed"
        );
    }

    private static void expectOrderError(Runnable action) {
        try {
            action.run();

        } catch (OrderException expected) {
            return;
        }

        throw new AssertionError(
                "Expected OrderException"
        );
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}