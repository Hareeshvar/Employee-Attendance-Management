/**
 * Data Aggregation & Analytical Transformation Utilities
 * Derived purely from real backend API objects (AttendanceResponseDTO).
 */

/**
 * Format a JavaScript Date object as YYYY-MM-DD using local time (prevents UTC timezone shifts).
 */
export const getLocalDateString = (dateObj) => {
  const d = new Date(dateObj);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

/**
 * Filter attendance records by range: '7D' (last 7 days), '30D' (last 30 days) or 'ALL'
 */
export const filterRecordsByRange = (records = [], range = '7D') => {
  if (!Array.isArray(records) || records.length === 0) return [];

  const cutoff = new Date();

  if (range === '7D') {
    cutoff.setDate(cutoff.getDate() - 7);
  } else if (range === '30D') {
    cutoff.setDate(cutoff.getDate() - 30);
  } else {
    return records;
  }

  const cutoffStr = getLocalDateString(cutoff);
  return records.filter((r) => r.attendanceDate >= cutoffStr);
};

/**
 * Calculate Attendance Rate (%) based on real records
 * Formula: Attended Days = (PRESENT * 1.0) + (LATE * 1.0) + (HALF_DAY * 0.5)
 * Total Logs = PRESENT + LATE + ABSENT + HALF_DAY
 */
export const calculateAttendanceRate = (records = []) => {
  if (!Array.isArray(records) || records.length === 0) {
    return { rate: 0, presentCount: 0, lateCount: 0, absentCount: 0, halfDayCount: 0, total: 0 };
  }

  let presentCount = 0;
  let lateCount = 0;
  let absentCount = 0;
  let halfDayCount = 0;

  records.forEach((r) => {
    const status = String(r.status || '').toUpperCase();
    if (status === 'PRESENT') presentCount++;
    else if (status === 'LATE') lateCount++;
    else if (status === 'ABSENT') absentCount++;
    else if (status === 'HALF_DAY') halfDayCount++;
  });

  const total = presentCount + lateCount + absentCount + halfDayCount;
  if (total === 0) {
    return { rate: 0, presentCount, lateCount, absentCount, halfDayCount, total: 0 };
  }

  const weighted = presentCount * 1.0 + lateCount * 1.0 + halfDayCount * 0.5;
  const rate = Math.round((weighted / total) * 1000) / 10; // 1 decimal precision

  return { rate, presentCount, lateCount, absentCount, halfDayCount, total };
};

/**
 * Build daily time-series dataset for Attendance Trend Chart
 */
export const buildDailyTrendData = (records = [], range = '7D') => {
  const daysCount = range === '30D' ? 30 : 7;
  const result = [];

  const today = new Date();

  for (let i = daysCount - 1; i >= 0; i--) {
    const d = new Date();
    d.setDate(today.getDate() - i);
    const dateStr = getLocalDateString(d);

    // Format display label e.g., "Sep 10" or "Wed"
    const label = d.toLocaleDateString('en-US', {
      month: range === '30D' ? 'short' : undefined,
      weekday: range === '7D' ? 'short' : undefined,
      day: 'numeric',
    });

    const dayRecords = records.filter((r) => r.attendanceDate === dateStr);

    let present = 0;
    let late = 0;
    let absent = 0;
    let halfDay = 0;

    dayRecords.forEach((r) => {
      const status = String(r.status || '').toUpperCase();
      if (status === 'PRESENT') present++;
      else if (status === 'LATE') late++;
      else if (status === 'ABSENT') absent++;
      else if (status === 'HALF_DAY') halfDay++;
    });

    result.push({
      date: dateStr,
      label,
      present,
      late,
      absent,
      halfDay,
      total: dayRecords.length,
    });
  }

  return result;
};

/**
 * Group attendance data by Department (for Admin & HR analytics)
 */
export const buildDepartmentComparisonData = (records = []) => {
  if (!Array.isArray(records) || records.length === 0) return [];

  const map = {};

  records.forEach((r) => {
    const deptName = r.departmentName || (r.departmentId ? `Dept #${r.departmentId}` : 'General');
    if (!map[deptName]) {
      map[deptName] = { deptName, records: [] };
    }
    map[deptName].records.push(r);
  });

  return Object.values(map)
    .map(({ deptName, records: deptRecords }) => {
      const { rate, presentCount, lateCount, absentCount, total } = calculateAttendanceRate(deptRecords);
      return {
        deptName,
        rate,
        total,
        presentCount,
        lateCount,
        absentCount,
      };
    })
    .sort((a, b) => b.rate - a.rate);
};

/**
 * Calculate Average Working Hours (for Employee / Personal analytics)
 */
export const calculateAvgWorkingHours = (records = []) => {
  const valid = records.filter((r) => r.workingHours !== null && r.workingHours !== undefined && r.workingHours > 0);
  if (valid.length === 0) return 0;

  const sum = valid.reduce((acc, curr) => acc + Number(curr.workingHours), 0);
  return Math.round((sum / valid.length) * 10) / 10;
};
