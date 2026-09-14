package com.hareeshvar.attendance.entity;

import java.time.LocalDateTime;

import com.hareeshvar.attendance.enums.PunchType;
import com.hareeshvar.attendance.enums.VerificationMethod;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "attendance_location_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceLocationVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    @Enumerated(EnumType.STRING)
    @Column(name = "punch_type", nullable = false, length = 20)
    private PunchType punchType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "workplace_id")
    private Workplace workplace;

    @Column(name = "distance_meters")
    private Double distanceMeters;

    @Column(name = "location_accuracy_meters")
    private Double locationAccuracyMeters;

    @Builder.Default
    @Column(name = "location_verified", nullable = false)
    private Boolean locationVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_method", nullable = false, length = 30)
    private VerificationMethod verificationMethod;

    @Column(name = "verified_at", nullable = false)
    private LocalDateTime verifiedAt;
}
