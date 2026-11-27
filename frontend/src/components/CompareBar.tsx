import { useNavigate } from 'react-router-dom'
import type { Project } from '../types/project'
import './CompareBar.css'

export const COMPARE_SESSION_KEY = 'oss-atlas-compare'

interface Props {
  projects: Project[]
  onRemove: (id: string) => void
  onClear: () => void
}

export function CompareBar({ projects, onRemove, onClear }: Props) {
  const navigate = useNavigate()
  if (projects.length === 0) return null

  function handleCompare() {
    sessionStorage.setItem(COMPARE_SESSION_KEY, JSON.stringify(projects))
    navigate('/compare')
  }

  return (
    <div className="compare-bar">
      <span className="compare-bar-label">COMPARE</span>
      <div className="compare-bar-chips">
        {projects.map(p => (
          <span key={p.id} className="compare-bar-chip">
            {p.name}
            <button
              className="compare-bar-remove"
              onClick={() => onRemove(p.id)}
              aria-label={`Remove ${p.name}`}
            >×</button>
          </span>
        ))}
        {projects.length < 3 && (
          <span className="compare-bar-hint">Add project to compare</span>
        )}
      </div>
      <div className="compare-bar-actions">
        <button className="compare-bar-clear" onClick={onClear}>Clear</button>
        <button
          className="compare-bar-btn"
          onClick={handleCompare}
          disabled={projects.length < 2}
        >
          Compare {projects.length}
        </button>
      </div>
    </div>
  )
}
