import './ErrorState.css'

interface Props {
  message?: string
  onRetry?: () => void
}

export function ErrorState({ message = 'Something went wrong.', onRetry }: Props) {
  return (
    <div className="error-state">
      <svg className="error-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="1.5"/>
        <path d="M12 7v5M12 15.5v.5" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round"/>
      </svg>
      <p>{message}</p>
      {onRetry && (
        <button className="error-retry" onClick={onRetry}>
          Try again
        </button>
      )}
    </div>
  )
}
