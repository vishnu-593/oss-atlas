import type { Project } from '../types/project'
import './ComparisonDrawer.css'

function fmt(n: number): string {
  if (n >= 1000) return `${(n / 1000).toFixed(1)}k`
  return String(n)
}

function getHealth(lastUpdated: string): { label: string; color: string } {
  if (!lastUpdated) return { label: '—', color: 'var(--text-muted)' }
  const months = (Date.now() - new Date(lastUpdated).getTime()) / (1000 * 60 * 60 * 24 * 30)
  if (months <= 3)  return { label: 'Active',   color: '#4ade80' }
  if (months <= 12) return { label: 'Healthy',  color: '#86efac' }
  if (months <= 24) return { label: 'Slow',     color: '#fbbf24' }
  return                   { label: 'Inactive', color: '#f87171' }
}

interface Props {
  projects: Project[]
  onRemove: (id: string) => void
  onClear: () => void
}

export function ComparisonDrawer({ projects, onRemove, onClear }: Props) {
  if (projects.length === 0) return null

  return (
    <div className="cmp-drawer">
      <div className="cmp-toolbar">
        <span className="cmp-toolbar-label">
          Comparing {projects.length} project{projects.length !== 1 ? 's' : ''}
          {projects.length === 1 && ' — select one more to compare'}
        </span>
        <button className="cmp-clear" onClick={onClear}>Clear all</button>
      </div>

      {projects.length >= 2 ? (
        <div className="cmp-table-wrap">
          <table className="cmp-table">
            <thead>
              <tr>
                <th className="cmp-th-label" />
                {projects.map(p => (
                  <th key={p.id} className="cmp-th">
                    <span className="cmp-project-name" title={p.name}>{p.name}</span>
                    <button
                      className="cmp-remove"
                      onClick={() => onRemove(p.id)}
                      title="Remove from comparison"
                    >×</button>
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              <tr>
                <td className="cmp-row-label">Stars</td>
                {projects.map(p => <td key={p.id} className="cmp-cell">{fmt(p.stars)}</td>)}
              </tr>
              <tr>
                <td className="cmp-row-label">Forks</td>
                {projects.map(p => <td key={p.id} className="cmp-cell">{fmt(p.forks)}</td>)}
              </tr>
              <tr>
                <td className="cmp-row-label">Language</td>
                {projects.map(p => <td key={p.id} className="cmp-cell">{p.language || '—'}</td>)}
              </tr>
              <tr>
                <td className="cmp-row-label">License</td>
                {projects.map(p => <td key={p.id} className="cmp-cell">{p.license}</td>)}
              </tr>
              <tr>
                <td className="cmp-row-label">Health</td>
                {projects.map(p => {
                  const { label, color } = getHealth(p.lastUpdated)
                  return <td key={p.id} className="cmp-cell" style={{ color }}>{label}</td>
                })}
              </tr>
              <tr>
                <td className="cmp-row-label">Last Updated</td>
                {projects.map(p => (
                  <td key={p.id} className="cmp-cell">
                    {p.lastUpdated ? new Date(p.lastUpdated).toLocaleDateString() : '—'}
                  </td>
                ))}
              </tr>
            </tbody>
          </table>
        </div>
      ) : (
        <p className="cmp-hint">Select one more project to start comparing</p>
      )}
    </div>
  )
}
