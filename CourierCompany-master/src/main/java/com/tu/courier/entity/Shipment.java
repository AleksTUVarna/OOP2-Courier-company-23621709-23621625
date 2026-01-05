package com.tu.courier.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Data
@NoArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- ВРЪЗКИ С ПОТРЕБИТЕЛИ (Може да са NULL, ако са Гости) ---

    @ManyToOne
    @JoinColumn(name = "sender_id") // Махнахме nullable = false
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id") // Махнахме nullable = false
    private User receiver;

    // --- ТЕКСТОВИ ДАННИ (Записваме ги винаги, независимо дали е Гост или Клиент) ---

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "sender_phone")
    private String senderPhone;

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "receiver_phone")
    private String receiverPhone;

    // --- ДАННИ ЗА ДОСТАВКА ---

    @Column(name = "delivery_address")
    private String deliveryAddress;

    // Връзка към Офис (ако е избрана опция "До Офис")
    @ManyToOne
    @JoinColumn(name = "to_office_id")
    private Office toOffice;

    // --- ПАРАМЕТРИ НА ПРАТКАТА ---

    private Double weight;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_type")
    private ShipmentType shipmentType;

    @Column(name = "shipment_date")
    private LocalDateTime shipmentDate;

    @Column(name = "tracking_id", unique = true, nullable = false)
    private String trackingId;

    // --- Helper methods for UI (support Guests) ---

    public String getSenderDisplayName() {
        if (sender != null && sender.getFullName() != null && !sender.getFullName().isBlank()) {
            return sender.getFullName();
        }
        return senderName != null && !senderName.isBlank() ? senderName : "Guest";
    }

    public String getReceiverDisplayName() {
        if (receiver != null && receiver.getFullName() != null && !receiver.getFullName().isBlank()) {
            return receiver.getFullName();
        }
        return receiverName != null && !receiverName.isBlank() ? receiverName : "Guest";
    }

    public String getSenderDisplayPhone() {
        if (sender != null && sender.getPhone() != null && !sender.getPhone().isBlank()) {
            return sender.getPhone();
        }
        return senderPhone != null ? senderPhone : "";
    }

    public String getReceiverDisplayPhone() {
        if (receiver != null && receiver.getPhone() != null && !receiver.getPhone().isBlank()) {
            return receiver.getPhone();
        }
        return receiverPhone != null ? receiverPhone : "";
    }

}