package com.posdb.sync.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "restaurant")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"apiKey", "userList"})
public class Restaurant extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column
    private String keyword;
    
    @Column
    private String description;
    
    @Column
    private String address;
    
    @Column
    private String phoneNumber;

    @Column(nullable = false, unique = true, length = 32)
    private String apiKey;

    @Column
    private Date createdAt;

    @Column(nullable = false, length = 20)
    private String status;

    @OneToMany(mappedBy = "restaurant")
    private List<User> userList;

    @PrePersist
    public void prePersist() {
        this.createdAt = new Date();
        this.status = "ACTIVE";
    }
}

