package com.example.LibDev.user.entity;

import com.example.LibDev.global.entity.BaseEntity;
import com.example.LibDev.user.entity.type.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private String name;

    private String phone;

    /*대출가능상태*/
    private boolean borrowAvailable;

    /*패널티만료일*/
    private LocalDateTime penaltyExpiration;

    /*탈퇴여부*/
    private boolean withdraw;

    @Enumerated(EnumType.STRING)
    private Role role;

    public void update(String email, String name, String phone) {
        this.email = email;

        this.name = name;
        this.phone = phone;
    }

    public void updatePassword(String password) {this.password = password;}

    public void updateBorrowAvailable(Boolean borrowAvailable) {this.borrowAvailable = borrowAvailable;}

}
