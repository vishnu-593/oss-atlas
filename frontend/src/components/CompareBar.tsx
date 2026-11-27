import { useNavigate } from 'react-router-dom'
import { useCompare } from '../context/CompareContext'
import './CompareBar.css'

export function CompareBar() {
  const { selected, remove, clear } = useCompare()
  const navigate = useNavigate()

  if (selected.length === 0) return null

  return (
    <div className="compare-bar">
      <div className="compare-bar-inner">
        <span className="compare-bar-label">Compare</span>
        <div className="compare-bar-chips">
          {selected.map(p => (
            <div key={p.id} className="compare-chip">
              <span className="compare-chip-name">{p.name}</span>
              <button
                className="compare-chip-remove"
                onClick={() => remove(p.id)}
                aria-label={`Remove ${p.name}`}
              >
                ×
              </button>
            </div>
          ))}
          {Array.from({ length: 3 - selected.length }).map((_, i) => (
            <div key={`empty-${i}`} className="compare-chip compare-chip-empty">
              <span>Add project</span>
            </div>
          ))}
        </div>
        <div className="compare-bar-actions">
          <button className="compare-clear-btn" onClick={clear}>Clear</button>
          <button
            className="compare-go-btn"
            disabled={selected.length < 2}
            onClick={() => navigate('/compare')}
          >
            Compare {selected.length}
          </button>
        </div>
      </div>
    </div>
  )
}
