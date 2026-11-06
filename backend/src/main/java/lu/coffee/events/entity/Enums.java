package lu.coffee.events.entity;

public final class Enums {
    private Enums() {}
    public enum Role { CUSTOMER, KITCHEN, ADMIN }
    public enum OrderMode { DINE_IN, TAKEAWAY }
    public enum OrderStatus { NEW, PREPARING, READY, SERVED, CANCELLED }
    public enum ReservationStatus { PENDING, CONFIRMED, REFUSED, CANCELLED, COMPLETED }
}
