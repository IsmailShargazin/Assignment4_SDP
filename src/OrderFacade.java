import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OrderFacade {

    private static class StoredOrder {

        private final Customer customer;
        private final OrderReceipt receipt;

        public StoredOrder(Customer customer, OrderReceipt receipt) {
            this.customer = customer;
            this.receipt = receipt;
        }

        public Customer customer() {
            return customer;
        }

        public OrderReceipt receipt() {
            return receipt;
        }
    }

    private final InventoryService inventory;
    private final PaymentProcessor payments;
    private final ShippingService shipping;
    private final NotificationService notifications;

    private final Map<String, StoredOrder> orders = new HashMap<>();

    private int nextOrderNumber = 1;

    public OrderFacade() {
        this(
                new InventoryService(defaultStock()),

                new PaymentProcessor(
                        Map.of(
                                "alice@example.com", 30_000L,
                                "bob@example.com", 1_000L
                        )
                ),

                new ShippingService(),
                new NotificationService()
        );
    }

    // Package-private constructor for test setup.
    OrderFacade(
            InventoryService inventory,
            PaymentProcessor payments,
            ShippingService shipping,
            NotificationService notifications
    ) {
        this.inventory = Objects.requireNonNull(inventory);
        this.payments = Objects.requireNonNull(payments);
        this.shipping = Objects.requireNonNull(shipping);
        this.notifications = Objects.requireNonNull(notifications);
    }

    private static Map<Product, Integer> defaultStock() {
        Map<Product, Integer> stock = new LinkedHashMap<>();

        stock.put(
                new Product("P1", "Keyboard", 10_000),
                5
        );

        stock.put(
                new Product("P2", "Mouse", 5_000),
                3
        );

        return stock;
    }

    public List<Product> getProducts() {
        return inventory.getProducts();
    }

    public OrderReceipt placeOrder(Cart cart, Customer customer) {
        Objects.requireNonNull(cart, "Cart is required.");
        Objects.requireNonNull(customer, "Customer is required.");

        if (cart.isEmpty()) {
            throw new OrderException(
                    "Cannot place an empty order."
            );
        }

        Cart snapshot = cart.copy();

        long total = snapshot.totalTenge();

        String orderId = "ORD-" + nextOrderNumber++;

        OrderReceipt receipt;

        try {
            inventory.reserve(orderId, snapshot);

            payments.charge(orderId, customer, total);

            String shipmentId = shipping.create(orderId, customer);

            receipt = new OrderReceipt(
                    orderId,
                    total,
                    shipmentId,
                    OrderReceipt.Status.PLACED
            );

            orders.put(
                    orderId,
                    new StoredOrder(customer, receipt)
            );

        } catch (OrderException exception) {
            rollback(orderId);

            notifications.send(
                    customer,
                    "Order " + orderId
                            + " failed: " + exception.getMessage()
            );

            throw exception;
        }

        notifications.send(
                customer,
                "Order " + orderId
                        + " placed. Total: " + total + " KZT."
        );

        return receipt;
    }

    public OrderReceipt cancelOrder(String orderId) {
        StoredOrder stored = orders.get(orderId);

        if (stored == null) {
            throw new OrderException(
                    "Order not found: " + orderId
            );
        }

        if (stored.receipt().status()
                == OrderReceipt.Status.CANCELLED) {

            throw new OrderException(
                    "Order is already cancelled: " + orderId
            );
        }

        rollback(orderId);

        OrderReceipt previous = stored.receipt();

        OrderReceipt cancelled = new OrderReceipt(
                orderId,
                previous.totalTenge(),
                previous.shipmentId(),
                OrderReceipt.Status.CANCELLED
        );

        orders.put(
                orderId,
                new StoredOrder(stored.customer(), cancelled)
        );

        notifications.send(
                stored.customer(),
                "Order " + orderId + " cancelled and refunded."
        );

        return cancelled;
    }

    private void rollback(String orderId) {
        shipping.cancel(orderId);
        payments.refund(orderId);
        inventory.release(orderId);
    }
}