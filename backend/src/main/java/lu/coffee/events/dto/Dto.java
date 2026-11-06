package lu.coffee.events.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lu.coffee.events.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public final class Dto {
    private Dto() {}

    public record ErrorResponse(String message, Map<String, String> fields) {}

    public record RegisterRequest(@NotBlank String name, @Email @NotBlank String email,
                                  @Size(min = 8, message = "8 caractères minimum") String password, String phone) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record AuthResponse(String token, User user) {}

    public record ProfileRequest(@NotBlank String name, String phone) {}

    public record OrderItemReq(@NotNull Long productId, @Min(1) @Max(50) int quantity,
                               List<String> choices, String note) {}

    public record OrderRequest(@NotNull Enums.OrderMode mode, String tableLabel, @NotBlank String customerName,
                               String phone, String email, LocalDateTime pickupAt, String note,
                               @NotEmpty @Valid List<OrderItemReq> items) {}

    public record StatusRequest(String status, String adminNote) {}

    public record AvailableRequest(boolean available) {}

    public record PaidRequest(boolean paid) {}

    public record ReservationRequest(@NotBlank String customerName, @NotBlank @Email String email,
                                     @NotBlank String phone, String eventType, @NotNull LocalDate date,
                                     @NotNull LocalTime startTime, @NotNull LocalTime endTime,
                                     @Min(1) int guests, Long packageId, List<Long> extraIds, String note,
                                     List<MealReq> meals) {}

    /** Plat ou boisson réservé pour la salle : soit une quantité précise, soit un par invité. */
    public record MealReq(@NotNull Long productId, boolean perGuest, @Min(0) @Max(500) int quantity) {}

    public record DayInfo(LocalDate date, boolean closed, String reason, List<String> booked) {}

    public record Menu(List<Category> categories, List<Product> products) {}

    public record RoomInfo(List<RoomPackage> packages, List<RoomExtra> extras) {}

    public record StaffRequest(String name, String email, String password, String phone,
                               Enums.Role role, Boolean active) {}
}
