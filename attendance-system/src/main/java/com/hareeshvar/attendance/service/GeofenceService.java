package com.hareeshvar.attendance.service;

import com.hareeshvar.attendance.entity.Workplace;

public interface GeofenceService {

    double calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2);

    GeofenceResult verifyLocation(Long userId, Double latitude, Double longitude, Double accuracyMeters);

    record GeofenceResult(Workplace workplace, double distanceMeters, double accuracyMeters) {}
}
