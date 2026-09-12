package com.hareeshvar.attendance.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.hareeshvar.attendance.enums.AttendanceStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attendanceId;

   @ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "user_id", nullable = false)
private User user;

@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "department_id", nullable = false)
private Department department;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    @Column(nullable = false)
    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift;

    private Double workingHours;

    @Builder.Default
    @Column(name = "late_minutes", nullable = false)
    private Integer lateMinutes = 0;

    @Builder.Default
    @Column(name = "early_departure_minutes", nullable = false)
    private Integer earlyDepartureMinutes = 0;

    @Builder.Default
    @Column(name = "working_minutes", nullable = false)
    private Integer workingMinutes = 0;

    @Builder.Default
    @Column(name = "overtime_minutes", nullable = false)
    private Integer overtimeMinutes = 0;

    @Builder.Default
    @Column(name = "exceptions_json", nullable = false)
    private String exceptionsJson = "[]";

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}