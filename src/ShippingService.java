import java.util.HashMap;
import java.util.Map;

public class ShippingService {

    public static class Shipment {

        private final String id;
        private final String address;

        public Shipment(String id, String address) {
            this.id = id;
            this.address = address;
        }

        public String id() {
            return id;
        }

        public String address() {
            return address;
        }
    }

    private final Map<String, Shipment> shipments = new HashMap<>();

    private int nextShipmentNumber = 1;

    public String create(String orderId, Customer customer) {
        if (shipments.containsKey(orderId)) {
            throw new OrderException(
                    "Shipment already exists."
            );
        }

        String shipmentId = "SHIP-" + nextShipmentNumber++;

        shipments.put(
                orderId,
                new Shipment(shipmentId, customer.address())
        );

        System.out.println(
                "Shipping: " + shipmentId
                        + " to " + customer.address()
        );

        return shipmentId;
    }

    public boolean hasShipment(String orderId) {
        return shipments.containsKey(orderId);
    }

    public void cancel(String orderId) {
        if (shipments.remove(orderId) != null) {
            System.out.println(
                    "Shipping: cancelled delivery for " + orderId
            );
        }
    }
}