package com.tu.courier.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data // Lombok прави getters, setters автоматично
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_no", unique = true)
    private Integer clientNo;

    @Column(name = "courier_no", unique = true)
    private Integer courierNo;


    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", unique = true)
    private String phone;

    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }

    @ManyToOne
    @JoinColumn(name = "office_id")
    private Office office;


}