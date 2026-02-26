package com.posdb.sync.entity;

import com.posdb.sync.entity.enums.UserRole;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "passwordHash")
public class User extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", referencedColumnName = "id")
    private Restaurant restaurant;

    @Column(nullable = false, length = 255)
    private String email;

    @Column
    private String mobileNumber;

    @Column
    private String fullName;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false)
    private Boolean mustChangePassword;

    @Column
    private Date createdAt;

    @Column
    private Date updatedAt;



    @PrePersist
    public void prePersist() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.mustChangePassword = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = new Date();
    }
}

