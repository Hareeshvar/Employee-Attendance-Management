// Extract human-friendly error messages from backend Axios error responses
export const getErrorMessage = (error) => {
  if (!error) return 'An unknown error occurred.';
  if (typeof error === 'string') return error;

  // Backend GlobalExceptionHandler error response
  if (error.response?.data?.message) {
    return error.response.data.message;
  }
  if (error.response?.data?.error) {
    return `${error.response.data.error}: ${error.response.data.message || ''}`;
  }
  if (error.response?.data && typeof error.response.data === 'string') {
    return error.response.data;
  }
  if (error.message) {
    if (error.message === 'Network Error') {
      return 'Unable to reach backend server. Please verify Spring Boot (port 8080) and MySQL are running.';
    }
    return error.message;
  }
  return 'Failed to execute operation. Please check network connection.';
};

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
