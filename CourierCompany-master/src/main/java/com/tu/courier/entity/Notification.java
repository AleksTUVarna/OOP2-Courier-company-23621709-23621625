package com.tu.courier.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // За кого е известието (null = global)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Към коя пратка (може да е null, но при вас ще го даваме)
    @ManyToOne
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_read", nullable = false)
    private boolean read;



    public Notification() {
        // Hibernate го иска
    }

    // ✅ Това ти оправя грешката
    public Notification(User user, Shipment shipment, String message) {
        this.user = user;
        this.shipment = shipment;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }

    // getters / setters
    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Shipment getShipment() { return shipment; }
    public void setShipment(Shipment shipment) { this.shipment = shipment; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
