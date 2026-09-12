// Normalize backend Axios error responses into a canonical error object
export const normalizeApiError = (error) => {
  if (!error) {
    return {
      message: 'An unknown error occurred.',
      status: 500,
      errorCode: 'UNKNOWN',
      fieldErrors: null,
      requestId: null,
    };
  }

  if (typeof error === 'string') {
    return {
      message: error,
      status: 400,
      errorCode: 'BAD_REQUEST',
      fieldErrors: null,
      requestId: null,
    };
  }

  const responseData = error.response?.data;
  const status = error.response?.status || 500;
  const requestId = error.response?.headers?.['x-request-id'] || responseData?.requestId || null;

  let fieldErrors = responseData?.fieldErrors || null;
  let message = responseData?.message || error.message || 'An unexpected error occurred.';

  if (responseData?.fieldErrors && Object.keys(responseData.fieldErrors).length > 0) {
    const details = Object.entries(responseData.fieldErrors)
      .map(([field, msg]) => `${field}: ${msg}`)
      .join(', ');
    message = `Validation failed (${details})`;
  } else if (error.message === 'Network Error') {
    message = 'Unable to reach backend server. Please verify Spring Boot (port 8080) and MySQL are running.';
  }

  return {
    message,
    status,
    errorCode: responseData?.errorCode || (status === 401 ? 'UNAUTHORIZED' : status === 403 ? 'ACCESS_DENIED' : 'ERROR'),
    fieldErrors,
    requestId,
  };
};

// Extract human-friendly error messages for UI display
export const getErrorMessage = (error) => normalizeApiError(error).message;

// Format ISO date (YYYY-MM-DD) to readable format
export const formatDate = (dateStr) => {
  if (!dateStr) return 'N/A';
  try {
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    return d.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  } catch (e) {
    return dateStr;
  }
};

// Format Time (HH:mm:ss or HH:mm)
export const formatTime = (timeStr) => {
  if (!timeStr) return '--:--';
  if (typeof timeStr === 'string' && timeStr.length <= 8) {
    const parts = timeStr.split(':');
    if (parts.length >= 2) {
      let hours = parseInt(parts[0], 10);
      const minutes = parts[1];
      const ampm = hours >= 12 ? 'PM' : 'AM';
      hours = hours % 12 || 12;
      return `${hours}:${minutes} ${ampm}`;
    }
  }
  return timeStr;
};

// Format Currency
export const formatCurrency = (amount) => {
  if (amount === undefined || amount === null) return '$0.00';
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(amount);
};

// Format Minutes into human readable hours and minutes (e.g. 509 -> 8h 29m)
export const formatMinutes = (minutes) => {
  if (minutes === undefined || minutes === null || isNaN(minutes) || minutes <= 0) return '0m';
  const hrs = Math.floor(minutes / 60);
  const mins = minutes % 60;
  if (hrs > 0 && mins > 0) return `${hrs}h ${mins}m`;
  if (hrs > 0) return `${hrs}h`;
  return `${mins}m`;
};
