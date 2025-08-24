package com.sistema_de_inventarios_v02.audit;

import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import jakarta.persistence.*;

@Entity
@Table(name = "REVINFO")
@RevisionEntity(CustomRevisionListener.class)
public class CustomRevisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    @Column(name = "REV")  // Mapeo explícito a la columna REV
    private int rev;

    @RevisionTimestamp
    @Column(name = "REVTSTMP")  // Mapeo explícito a la columna REVTSTMP
    private long timestamp;

    @Column(name = "username")
    private String username;

    @Column(name = "user_id")
    private Long userId;

    // Default constructor
    public CustomRevisionEntity() {
    }

    // Getters and Setters para compatibilidad con DefaultRevisionEntity
    public int getId() {
        return rev;
    }

    public void setId(int rev) {
        this.rev = rev;
    }

    public int getRev() {
        return rev;
    }

    public void setRev(int rev) {
        this.rev = rev;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomRevisionEntity)) return false;
        CustomRevisionEntity that = (CustomRevisionEntity) o;
        return rev == that.rev;
    }

    @Override
    public int hashCode() {
        return rev;
    }

    @Override
    public String toString() {
        return "CustomRevisionEntity{" +
                "rev=" + rev +
                ", timestamp=" + timestamp +
                ", username='" + username + '\'' +
                ", userId=" + userId +
                '}';
    }
}