public class OrderReceipt {

    public enum Status {
        PLACED,
        CANCELLED
    }

    private final String orderId;
    private final long totalTenge;
    private final String shipmentId;
    private final Status status;

    public OrderReceipt(
            String orderId,
            long totalTenge,
            String shipmentId,
            Status status
    ) {
        this.orderId = orderId;
        this.totalTenge = totalTenge;
        this.shipmentId = shipmentId;
        this.status = status;
    }

    public String orderId() {
        return orderId;
    }

    public long totalTenge() {
        return totalTenge;
    }

    public String shipmentId() {
        return shipmentId;
    }

    public Status status() {
        return status;
    }

    @Override
    public String toString() {
        return "OrderReceipt[orderId=" + orderId
                + ", totalTenge=" + totalTenge
                + ", shipmentId=" + shipmentId
                + ", status=" + status
                + "]";
    }
}