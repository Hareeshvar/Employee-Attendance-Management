/**
 * Geolocation Utility for Secure Geofenced Attendance
 * Wraps HTML5 navigator.geolocation in a Promise with fresh reading parameters.
 */
export const getCurrentLocation = (options = {}) => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject({
        code: 'NOT_SUPPORTED',
        message: 'Geolocation is not supported by your browser or device.',
      });
      return;
    }

    const defaultOptions = {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0,
      ...options,
    };

    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
          accuracyMeters: position.coords.accuracy,
        });
      },
      (error) => {
        let message = 'Unable to determine your location. Please try again.';
        let code = 'UNKNOWN_ERROR';

        switch (error.code) {
          case error.PERMISSION_DENIED:
            code = 'PERMISSION_DENIED';
            message = 'Location permission was denied. Please allow location access in your browser address bar to record attendance.';
            break;
          case error.POSITION_UNAVAILABLE:
            code = 'POSITION_UNAVAILABLE';
            message = 'Location information is unavailable on your device. Please ensure GPS or Wi-Fi is enabled.';
            break;
          case error.TIMEOUT:
            code = 'TIMEOUT';
            message = 'Location request timed out. Please verify device GPS is enabled and try again.';
            break;
          default:
            break;
        }

        reject({ code, message, originalError: error });
      },
      defaultOptions
    );
  });
};
