import { useNavigate } from 'react-router-dom'
import { useCompare } from '../context/CompareContext'
import type { Project } from '../types/project'
import './ComparePage.css'

function fmt(n?: number): string {
  if (n === undefined || n === null) return '—'
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1)}M`
  if (n >= 1_000) return `${(n / 1_000).toFixed(1)}k`
  return String(n)
}

function fmtDate(iso?: string): string {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })
}

function healthClass(label?: string): string {
  switch (label) {
    case 'Healthy':      return 'health-healthy'
    case 'Active':       return 'health-active'
    case 'Moderate':     return 'health-moderate'
    case 'Low Activity': return 'health-low'
    case 'Inactive':     return 'health-inactive'
    default:             return ''
  }
}

function bestIdx(projects: Project[], key: keyof Project, prefer: 'max' | 'min'): number {
  let best = -1
  let bestVal: number | undefined
  projects.forEach((p, i) => {
    const v = p[key] as number | undefined
    if (v === undefined || v === null) return
    if (bestVal === undefined) { best = i; bestVal = v; return }
    if (prefer === 'max' && v > bestVal) { best = i; bestVal = v }
    if (prefer === 'min' && v < bestVal) { best = i; bestVal = v }
  })
  return best
}

function bestDateIdx(projects: Project[]): number {
  let best = -1
  let bestMs = -Infinity
  projects.forEach((p, i) => {
    if (!p.lastUpdated) return
    const ms = new Date(p.lastUpdated).getTime()
    if (ms > bestMs) { best = i; bestMs = ms }
  })
  return best
}

export function ComparePage() {
  const { selected, clear } = useCompare()
  const navigate = useNavigate()

  if (selected.length < 2) {
    return (
      <div className="compare-empty">
        <h2>Select at least 2 projects to compare</h2>
        <p>Use the + button on any project card in Explore or Discover.</p>
        <button className="compare-back-btn" onClick={() => navigate('/explore')}>Browse Catalogue</button>
      </div>
    )
  }

  const starsWinner    = bestIdx(selected, 'stars', 'max')
  const forksWinner    = bestIdx(selected, 'forks', 'max')
  const issuesWinner   = bestIdx(selected, 'openIssues', 'min')
  const scoreWinner    = bestIdx(selected, 'healthScore', 'max')
  const updatedWinner  = bestDateIdx(selected)

  return (
    <div className="compare-page">
      <div className="compare-page-header">
        <div>
          <h1>Project Comparison</h1>
          <p className="compare-subtitle">Side-by-side across {selected.length} projects</p>
        </div>
        <div className="compare-header-actions">
          <button className="compare-back-btn" onClick={() => navigate(-1)}>← Back</button>
          <button className="compare-clear-link" onClick={() => { clear(); navigate('/explore') }}>
            Clear &amp; start over
          </button>
        </div>
      </div>

      <div className="compare-table-wrap">
        <table className="compare-table">
          <thead>
            <tr>
              <th className="metric-col"></th>
              {selected.map(p => (
                <th key={p.id} className="project-col">
                  <div className="compare-project-head">
                    <span className="compare-project-name">{p.name}</span>
                    <span className="compare-project-owner">by {p.owner}</span>
                    {p.healthLabel && (
                      <span className={`compare-health-badge health-badge ${healthClass(p.healthLabel)}`}>
                        {p.healthLabel}
                      </span>
                    )}
                  </div>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>

            {/* Health Score */}
            <tr className="metric-row">
              <td className="metric-label">
                <span className="metric-name">Health Score</span>
                <span className="metric-hint">Out of 100</span>
              </td>
              {selected.map((p, i) => (
                <td key={p.id} className={`metric-val ${i === scoreWinner ? 'cell-best' : ''}`}>
                  <div className="score-cell">
                    <span className="score-number">{p.healthScore ?? '—'}</span>
                    <div className="score-bar-track">
                      <div
                        className="score-bar-fill"
                        style={{ width: `${p.healthScore ?? 0}%` }}
                      />
                    </div>
                  </div>
                  {i === scoreWinner && <span className="best-label">Best</span>}
                </td>
              ))}
            </tr>

            {/* Stars */}
            <tr className="metric-row">
              <td className="metric-label">
                <span className="metric-name">Stars</span>
                <span className="metric-hint">GitHub stars</span>
              </td>
              {selected.map((p, i) => (
                <td key={p.id} className={`metric-val ${i === starsWinner ? 'cell-best' : ''}`}>
                  {fmt(p.stars)}
                  {i === starsWinner && <span className="best-label">Most</span>}
                </td>
              ))}
            </tr>

            {/* Forks */}
            <tr className="metric-row">
              <td className="metric-label">
                <span className="metric-name">Forks</span>
                <span className="metric-hint">Community contributions</span>
              </td>
              {selected.map((p, i) => (
                <td key={p.id} className={`metric-val ${i === forksWinner ? 'cell-best' : ''}`}>
                  {fmt(p.forks)}
                  {i === forksWinner && <span className="best-label">Most</span>}
                </td>
              ))}
            </tr>

            {/* Open Issues */}
            <tr className="metric-row">
              <td className="metric-label">
                <span className="metric-name">Open Issues</span>
                <span className="metric-hint">Fewer is better</span>
              </td>
              {selected.map((p, i) => (
                <td key={p.id} className={`metric-val ${i === issuesWinner ? 'cell-best' : ''}`}>
                  {fmt(p.openIssues)}
                  {i === issuesWinner && <span className="best-label">Fewest</span>}
                </td>
              ))}
            </tr>

            {/* Last Updated */}
            <tr className="metric-row">
              <td className="metric-label">
                <span className="metric-name">Last Updated</span>
                <span className="metric-hint">Most recent activity</span>
              </td>
              {selected.map((p, i) => (
                <td key={p.id} className={`metric-val ${i === updatedWinner ? 'cell-best' : ''}`}>
                  {fmtDate(p.lastUpdated)}
                  {i === updatedWinner && <span className="best-label">Newest</span>}
                </td>
              ))}
            </tr>

            {/* Language */}
            <tr className="metric-row">
              <td className="metric-label">
                <span className="metric-name">Language</span>
              </td>
              {selected.map(p => (
                <td key={p.id} className="metric-val">
                  <span className="compare-lang">{p.language || '—'}</span>
                </td>
              ))}
            </tr>

            {/* License */}
            <tr className="metric-row">
              <td className="metric-label">
                <span className="metric-name">License</span>
              </td>
              {selected.map(p => (
                <td key={p.id} className="metric-val">
                  <span className="compare-license">{p.license || '—'}</span>
                </td>
              ))}
            </tr>

            {/* Topics */}
            <tr className="metric-row">
              <td className="metric-label">
                <span className="metric-name">Topics</span>
              </td>
              {selected.map(p => (
                <td key={p.id} className="metric-val">
                  <div className="compare-topics">
                    {(p.topics ?? []).slice(0, 5).map(t => (
                      <span key={t} className="compare-topic-chip">{t}</span>
                    ))}
                    {(p.topics?.length ?? 0) === 0 && <span className="metric-empty">—</span>}
                  </div>
                </td>
              ))}
            </tr>

            {/* GitHub link */}
            <tr className="metric-row">
              <td className="metric-label">
                <span className="metric-name">Repository</span>
              </td>
              {selected.map(p => (
                <td key={p.id} className="metric-val">
                  <a
                    href={p.githubUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="compare-gh-link"
                  >
                    View on GitHub ↗
                  </a>
                </td>
              ))}
            </tr>

          </tbody>
        </table>
      </div>
    </div>
  )
}
