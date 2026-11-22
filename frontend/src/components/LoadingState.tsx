import './LoadingState.css'

interface Props {
  message?: string
}

export function LoadingState({ message = 'Loading...' }: Props) {
  return (
    <div className="loading-state">
      <div className="loading-spinner" aria-hidden="true" />
      <p>{message}</p>
    </div>
  )
}
