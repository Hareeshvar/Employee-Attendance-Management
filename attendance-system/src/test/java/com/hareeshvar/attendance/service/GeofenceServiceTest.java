package com.hareeshvar.attendance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hareeshvar.attendance.entity.EmployeeWorkplace;
import com.hareeshvar.attendance.entity.User;
import com.hareeshvar.attendance.entity.Workplace;
import com.hareeshvar.attendance.exception.BadRequestException;
import com.hareeshvar.attendance.exception.BusinessRuleViolationException;
import com.hareeshvar.attendance.repository.EmployeeWorkplaceRepository;
import com.hareeshvar.attendance.service.impl.GeofenceServiceImpl;

@ExtendWith(MockitoExtension.class)
class GeofenceServiceTest {

    @Mock
    private EmployeeWorkplaceRepository employeeWorkplaceRepository;

    private Clock fixedClock;
    private GeofenceServiceImpl geofenceService;

    // Test coordinates (Primary: College / SKCET, Secondary: Home Office)
    private static final double COLLEGE_LAT = 10.9390304;
    private static final double COLLEGE_LON = 76.9522005;
    private static final double HOME_LAT = 10.9979936;
    private static final double HOME_LON = 76.9548166;

    private Workplace collegeWorkplace;
    private Workplace homeWorkplace;
    private User testUser;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-09-14T09:00:00Z"), ZoneId.of("UTC"));
        geofenceService = new GeofenceServiceImpl(employeeWorkplaceRepository, fixedClock);

        testUser = User.builder().userId(1L).username("testuser").build();

        collegeWorkplace = Workplace.builder()
                .workplaceId(10L)
                .name("College Campus")
                .code("COLLEGE")
                .latitude(BigDecimal.valueOf(COLLEGE_LAT))
                .longitude(BigDecimal.valueOf(COLLEGE_LON))
                .radiusMeters(200.0)
                .maxAccuracyMeters(100.0)
                .isActive(true)
                .build();

        homeWorkplace = Workplace.builder()
                .workplaceId(20L)
                .name("Home Office")
                .code("HOME")
                .latitude(BigDecimal.valueOf(HOME_LAT))
                .longitude(BigDecimal.valueOf(HOME_LON))
                .radiusMeters(100.0)
                .maxAccuracyMeters(75.0)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Haversine: Identical coordinates should yield exactly 0 meters distance")
    void testHaversineZeroDistance() {
        double dist = geofenceService.calculateDistanceMeters(COLLEGE_LAT, COLLEGE_LON, COLLEGE_LAT, COLLEGE_LON);
        assertEquals(0.0, dist, 0.001);
    }

    @Test
    @DisplayName("Haversine: Distance between College and Home Office should be approx 6.56km")
    void testHaversineCollegeToHome() {
        double dist = geofenceService.calculateDistanceMeters(COLLEGE_LAT, COLLEGE_LON, HOME_LAT, HOME_LON);
        assertTrue(dist > 6500.0 && dist < 6650.0, "Expected ~6560m, but got: " + dist);
    }

    @Test
    @DisplayName("VerifyLocation: Inside College geofence with acceptable accuracy succeeds")
    void testVerifyLocationSuccess() {
        EmployeeWorkplace ew = EmployeeWorkplace.builder()
                .employeeWorkplaceId(101L)
                .user(testUser)
                .workplace(collegeWorkplace)
                .isPrimary(true)
                .status("ACTIVE")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        when(employeeWorkplaceRepository.findActiveAssignmentsOnDate(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(ew));

        // Coordinate 25m away from College center with 15m accuracy
        double testLat = COLLEGE_LAT + 0.0002;
        double testLon = COLLEGE_LON + 0.0001;

        GeofenceService.GeofenceResult result = geofenceService.verifyLocation(1L, testLat, testLon, 15.0);

        assertNotNull(result);
        assertEquals(collegeWorkplace.getWorkplaceId(), result.workplace().getWorkplaceId());
        assertTrue(result.distanceMeters() <= 200.0);
        assertEquals(15.0, result.accuracyMeters());
    }

    @Test
    @DisplayName("VerifyLocation: Outside all assigned workplaces throws BusinessRuleViolationException")
    void testVerifyLocationOutsideGeofence() {
        EmployeeWorkplace ew = EmployeeWorkplace.builder()
                .employeeWorkplaceId(101L)
                .user(testUser)
                .workplace(collegeWorkplace)
                .isPrimary(true)
                .status("ACTIVE")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        when(employeeWorkplaceRepository.findActiveAssignmentsOnDate(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(ew));

        // Coordinate far away (Chennai coordinates)
        double farLat = 13.0827;
        double farLon = 80.2707;

        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class, () ->
                geofenceService.verifyLocation(1L, farLat, farLon, 20.0));

        assertTrue(ex.getMessage().contains("outside your approved workplace area"));
    }

    @Test
    @DisplayName("VerifyLocation: Within geofence but accuracy exceeds threshold throws BusinessRuleViolationException")
    void testVerifyLocationAccuracyExceeded() {
        EmployeeWorkplace ew = EmployeeWorkplace.builder()
                .employeeWorkplaceId(101L)
                .user(testUser)
                .workplace(collegeWorkplace)
                .isPrimary(true)
                .status("ACTIVE")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        when(employeeWorkplaceRepository.findActiveAssignmentsOnDate(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(ew));

        // Right at center, but coarse accuracy (150m > 100m maxAccuracy)
        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class, () ->
                geofenceService.verifyLocation(1L, COLLEGE_LAT, COLLEGE_LON, 150.0));

        assertTrue(ex.getMessage().contains("location is not accurate enough"));
    }

    @Test
    @DisplayName("VerifyLocation: Employee with no active assignments throws BusinessRuleViolationException")
    void testVerifyLocationNoAssignments() {
        when(employeeWorkplaceRepository.findActiveAssignmentsOnDate(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of());

        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class, () ->
                geofenceService.verifyLocation(1L, COLLEGE_LAT, COLLEGE_LON, 20.0));

        assertTrue(ex.getMessage().contains("No active workplace assigned"));
    }

    @Test
    @DisplayName("VerifyLocation: Invalid or impossible coordinates throw BadRequestException")
    void testVerifyLocationInvalidCoordinates() {
        assertThrows(BadRequestException.class, () -> geofenceService.verifyLocation(1L, 95.0, 76.0, 20.0));
        assertThrows(BadRequestException.class, () -> geofenceService.verifyLocation(1L, 10.0, 195.0, 20.0));
        assertThrows(BadRequestException.class, () -> geofenceService.verifyLocation(1L, Double.NaN, 76.0, 20.0));
        assertThrows(BadRequestException.class, () -> geofenceService.verifyLocation(1L, 10.0, 76.0, -5.0));
    }

    @Test
    @DisplayName("VerifyLocation: Deterministic multi-workplace tie-breaking selects closest facility")
    void testMultiWorkplaceDeterministicSelection() {
        // Both College (200m radius) and another facility overlapping
        Workplace secondaryOverlap = Workplace.builder()
                .workplaceId(15L)
                .name("Secondary Hall")
                .code("HALL")
                .latitude(BigDecimal.valueOf(COLLEGE_LAT + 0.0005))
                .longitude(BigDecimal.valueOf(COLLEGE_LON + 0.0005))
                .radiusMeters(300.0)
                .maxAccuracyMeters(100.0)
                .isActive(true)
                .build();

        EmployeeWorkplace ewCollege = EmployeeWorkplace.builder()
                .employeeWorkplaceId(101L)
                .user(testUser)
                .workplace(collegeWorkplace)
                .isPrimary(true)
                .status("ACTIVE")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        EmployeeWorkplace ewHall = EmployeeWorkplace.builder()
                .employeeWorkplaceId(102L)
                .user(testUser)
                .workplace(secondaryOverlap)
                .isPrimary(false)
                .status("ACTIVE")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        when(employeeWorkplaceRepository.findActiveAssignmentsOnDate(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(ewCollege, ewHall));

        // Location is 20m from College, ~60m from Secondary Hall
        GeofenceService.GeofenceResult result = geofenceService.verifyLocation(1L, COLLEGE_LAT + 0.0001, COLLEGE_LON + 0.0001, 15.0);

        assertNotNull(result);
        assertEquals(collegeWorkplace.getWorkplaceId(), result.workplace().getWorkplaceId(),
                "Should deterministically select College as it is closer than Secondary Hall");
    }
}
