package com.hareeshvar.attendance.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hareeshvar.attendance.enums.LeaveStatus;
import com.hareeshvar.attendance.enums.LeaveType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leaveId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status;

    @Column(name = "total_days")
    private Integer totalDays;

    @Column(name = "leave_type_id")
    private Long leaveTypeId;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateTotalDays();
        syncLeaveTypeId();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotalDays();
        syncLeaveTypeId();
    }

    public void calculateTotalDays() {
        if (startDate != null && endDate != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
            this.totalDays = (int) Math.max(days, 1);
        }
    }

    public void syncLeaveTypeId() {
        if (leaveType != null) {
            this.leaveTypeId = (long) (leaveType.ordinal() + 1);
        }
    }
}