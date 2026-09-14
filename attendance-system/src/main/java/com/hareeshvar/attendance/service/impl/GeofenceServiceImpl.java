package com.hareeshvar.attendance.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hareeshvar.attendance.entity.EmployeeWorkplace;
import com.hareeshvar.attendance.entity.Workplace;
import com.hareeshvar.attendance.exception.BadRequestException;
import com.hareeshvar.attendance.exception.BusinessRuleViolationException;
import com.hareeshvar.attendance.repository.EmployeeWorkplaceRepository;
import com.hareeshvar.attendance.service.GeofenceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeofenceServiceImpl implements GeofenceService {

    private final EmployeeWorkplaceRepository employeeWorkplaceRepository;
    private final Clock clock;

    private static final int EARTH_RADIUS_METERS = 6_371_000;

    @Override
    public double calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    @Override
    @Transactional(readOnly = true)
    public GeofenceResult verifyLocation(Long userId, Double latitude, Double longitude, Double accuracyMeters) {
        validateCoordinates(latitude, longitude, accuracyMeters);

        LocalDate today = LocalDate.now(clock);
        List<EmployeeWorkplace> activeAssignments = employeeWorkplaceRepository.findActiveAssignmentsOnDate(userId, today);

        if (activeAssignments.isEmpty()) {
            throw new BusinessRuleViolationException("No active workplace assigned to employee");
        }

        List<MatchEvaluation> containedEvaluations = new ArrayList<>();
        boolean anyContained = false;
        double minRequiredAccuracy = Double.MAX_VALUE;

        for (EmployeeWorkplace assignment : activeAssignments) {
            Workplace wp = assignment.getWorkplace();
            if (wp == null || Boolean.FALSE.equals(wp.getIsActive())) {
                continue;
            }

            double distance = calculateDistanceMeters(
                    latitude,
                    longitude,
                    wp.getLatitude().doubleValue(),
                    wp.getLongitude().doubleValue()
            );

            boolean isContained = distance <= wp.getRadiusMeters();
            boolean isAccuracyAcceptable = accuracyMeters <= wp.getMaxAccuracyMeters();

            if (isContained) {
                anyContained = true;
                if (wp.getMaxAccuracyMeters() != null && wp.getMaxAccuracyMeters() < minRequiredAccuracy) {
                    minRequiredAccuracy = wp.getMaxAccuracyMeters();
                }
                if (isAccuracyAcceptable) {
                    containedEvaluations.add(new MatchEvaluation(assignment, wp, distance));
                }
            }
        }

        if (!anyContained) {
            throw new BusinessRuleViolationException("You are outside your approved workplace area. Please move closer and try again.");
        }

        if (containedEvaluations.isEmpty()) {
            String details = minRequiredAccuracy < Double.MAX_VALUE
                    ? " (reported: " + Math.round(accuracyMeters) + "m, required: ≤" + Math.round(minRequiredAccuracy) + "m)"
                    : "";
            throw new BusinessRuleViolationException("Your location is not accurate enough" + details + ". Please ensure Wi-Fi or location services are enabled, or ask an administrator to adjust the workplace's Max GPS Accuracy setting.");
        }

        // Deterministic Selection:
        // 1. Closest distance
        // 2. Primary assignment tie-breaker
        // 3. Smallest workplaceId tie-breaker
        containedEvaluations.sort(
                Comparator.comparingDouble(MatchEvaluation::distance)
                        .thenComparing(e -> Boolean.TRUE.equals(e.assignment().getIsPrimary()) ? 0 : 1)
                        .thenComparing(e -> e.workplace().getWorkplaceId())
        );

        MatchEvaluation bestMatch = containedEvaluations.get(0);
        double roundedDistance = Math.round(bestMatch.distance() * 100.0) / 100.0;

        return new GeofenceResult(bestMatch.workplace(), roundedDistance, accuracyMeters);
    }

    private void validateCoordinates(Double latitude, Double longitude, Double accuracyMeters) {
        if (latitude == null || latitude.isNaN() || !Double.isFinite(latitude) || latitude < -90.0 || latitude > 90.0) {
            throw new BadRequestException("Latitude must be between -90.0 and 90.0");
        }
        if (longitude == null || longitude.isNaN() || !Double.isFinite(longitude) || longitude < -180.0 || longitude > 180.0) {
            throw new BadRequestException("Longitude must be between -180.0 and 180.0");
        }
        if (accuracyMeters == null || accuracyMeters.isNaN() || !Double.isFinite(accuracyMeters) || accuracyMeters < 0.0) {
            throw new BadRequestException("Accuracy in meters must be non-negative");
        }
    }

    private record MatchEvaluation(EmployeeWorkplace assignment, Workplace workplace, double distance) {}
}
