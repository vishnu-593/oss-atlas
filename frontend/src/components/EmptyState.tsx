import './EmptyState.css'

interface Props {
  message?: string
}

export function EmptyState({ message = 'No projects found.' }: Props) {
  return (
    <div className="empty-state">
      <svg className="empty-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <circle cx="10.5" cy="10.5" r="7" stroke="currentColor" strokeWidth="1.5"/>
        <path d="M16 16l4.5 4.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
        <path d="M8 10.5h5M10.5 8v5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
      </svg>
      <p>{message}</p>
    </div>
  )
}
