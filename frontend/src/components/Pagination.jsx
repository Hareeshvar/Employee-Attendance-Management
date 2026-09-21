import React from 'react';
import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from 'lucide-react';

const Pagination = ({
  pageNumber = 0,
  pageSize = 10,
  totalElements = 0,
  totalPages = 1,
  first = true,
  last = true,
  onPageChange,
  onPageSizeChange,
  pageSizeOptions = [10, 25, 50, 100],
}) => {
  if (totalElements === 0) return null;

  const currentDisplayPage = pageNumber + 1;
  const startItem = pageNumber * pageSize + 1;
  const endItem = Math.min((pageNumber + 1) * pageSize, totalElements);

  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '0.75rem',
        padding: '0.85rem 1.25rem',
        background: 'rgba(15, 23, 42, 0.6)',
        borderTop: '1px solid var(--border-color)',
        borderBottomLeftRadius: 'var(--radius-lg)',
        borderBottomRightRadius: 'var(--radius-lg)',
        fontSize: '0.85rem',
        color: 'var(--text-secondary)',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
        <span>
          Showing <strong style={{ color: 'var(--text-primary)' }}>{startItem}</strong> to{' '}
          <strong style={{ color: 'var(--text-primary)' }}>{endItem}</strong> of{' '}
          <strong style={{ color: 'var(--text-primary)' }}>{totalElements}</strong> entries
        </span>

        {onPageSizeChange && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span>Per page:</span>
            <select
              className="form-select"
              style={{
                padding: '0.25rem 0.5rem',
                fontSize: '0.85rem',
                width: '75px',
                borderRadius: 'var(--radius-sm)',
              }}
              value={pageSize}
              onChange={(e) => onPageSizeChange(Number(e.target.value))}
            >
              {pageSizeOptions.map((opt) => (
                <option key={opt} value={opt}>
                  {opt}
                </option>
              ))}
            </select>
          </div>
        )}
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
        <button
          className="btn btn-outline"
          style={{ padding: '0.4rem 0.6rem' }}
          disabled={first || pageNumber === 0}
          onClick={() => onPageChange(0)}
          title="First Page"
        >
          <ChevronsLeft size={16} />
        </button>

        <button
          className="btn btn-outline"
          style={{ padding: '0.4rem 0.6rem' }}
          disabled={first || pageNumber === 0}
          onClick={() => onPageChange(pageNumber - 1)}
          title="Previous Page"
        >
          <ChevronLeft size={16} />
        </button>

        <span style={{ margin: '0 0.5rem', fontWeight: 600, color: 'var(--text-primary)' }}>
          Page {currentDisplayPage} of {totalPages}
        </span>

        <button
          className="btn btn-outline"
          style={{ padding: '0.4rem 0.6rem' }}
          disabled={last || pageNumber >= totalPages - 1}
          onClick={() => onPageChange(pageNumber + 1)}
          title="Next Page"
        >
          <ChevronRight size={16} />
        </button>

        <button
          className="btn btn-outline"
          style={{ padding: '0.4rem 0.6rem' }}
          disabled={last || pageNumber >= totalPages - 1}
          onClick={() => onPageChange(totalPages - 1)}
          title="Last Page"
        >
          <ChevronsRight size={16} />
        </button>
      </div>
    </div>
  );
};

export default Pagination;
