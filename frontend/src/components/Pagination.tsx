import './Pagination.css'

interface Props {
  page: number
  totalPages: number
  onPageChange: (page: number) => void
}

export function Pagination({ page, totalPages, onPageChange }: Props) {
  if (totalPages <= 1) return null

  return (
    <div className="pagination">
      <button
        className="page-btn"
        disabled={page === 0}
        onClick={() => onPageChange(page - 1)}
      >
        ← Prev
      </button>
      <span className="page-info">
        Page {page + 1} of {totalPages}
      </span>
      <button
        className="page-btn"
        disabled={page >= totalPages - 1}
        onClick={() => onPageChange(page + 1)}
      >
        Next →
      </button>
    </div>
  )
}
