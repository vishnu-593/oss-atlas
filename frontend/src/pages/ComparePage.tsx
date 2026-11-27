import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import type { Project } from '../types/project'
import { COMPARE_SESSION_KEY } from '../components/CompareBar'
import './ComparePage.css'

function fmt(n: number): string {
  if (n >= 1000) return `${(n / 1000).toFixed(1)}k`
  return String(n)
}

function healthScore(lastUpdated: string): number {
  if (!lastUpdated) return 0
  const days = Math.max(0, (Date.now() - new Date(lastUpdated).getTime()) / (1000 * 60 * 60 * 24))
  return Math.max(5, Math.min(100, Math.round(100 - days * 0.15)))
}

function healthLabel(lastUpdated: string): 'Active' | 'Healthy' | 'Slow' | 'Inactive' | 'Unknown' {
  if (!lastUpdated) return 'Unknown'
  const months = (Date.now() - new Date(lastUpdated).getTime()) / (1000 * 60 * 60 * 24 * 30)
  if (months <= 3)  return 'Active'
  if (months <= 12) return 'Healthy'
  if (months <= 24) return 'Slow'
  return 'Inactive'
}

const HEALTH_COLORS: Record<string, string> = {
  Active:   '#4ade80',
  Healthy:  '#86efac',
  Slow:     '#fbbf24',
  Inactive: '#f87171',
  Unknown:  'var(--text-muted)',
}

function bestIds(projects: Project[], getValue: (p: Project) => number, highest: boolean): Set<string> {
  if (projects.length <= 1) return new Set()
  const vals = projects.map(p => getValue(p))
  const target = highest ? Math.max(...vals) : Math.min(...vals)
  const winners = projects.filter(p => getValue(p) === target)
  if (winners.length === projects.length) return new Set()
  return new Set(winners.map(p => p.id))
}

function WinTag({ label }: { label: string }) {
  return <span className="cmp-win-tag">{label}</span>
}

export function ComparePage() {
  const navigate = useNavigate()
  const [projects, setProjects] = useState<Project[]>([])

  useEffect(() => {
    try {
      const raw = sessionStorage.getItem(COMPARE_SESSION_KEY)
      if (raw) setProjects(JSON.parse(raw) as Project[])
    } catch {}
  }, [])

  function handleClear() {
    sessionStorage.removeItem(COMPARE_SESSION_KEY)
    navigate('/explore')
  }

  function handleRemove(id: string) {
    const updated = projects.filter(p => p.id !== id)
    setProjects(updated)
    sessionStorage.setItem(COMPARE_SESSION_KEY, JSON.stringify(updated))
  }

  if (projects.length === 0) {
    return (
      <div className="cmp-page">
        <div className="cmp-inner cmp-empty-state">
          <p>No projects selected for comparison.</p>
          <button className="cmp-link" onClick={() => navigate('/explore')}>
            ← Go to Browse Catalogue
          </button>
        </div>
      </div>
    )
  }

  const scores = Object.fromEntries(projects.map(p => [p.id, healthScore(p.lastUpdated)]))
  const topScore    = bestIds(projects, p => scores[p.id], true)
  const topStars    = bestIds(projects, p => p.stars, true)
  const topForks    = bestIds(projects, p => p.forks, true)
  const fewestIssues = bestIds(projects, p => p.openIssues ?? Infinity, false)
  const newest      = bestIds(projects, p => new Date(p.lastUpdated || 0).getTime(), true)
  const hasIssues   = projects.some(p => p.openIssues !== undefined)
  const hasTopics   = projects.some(p => (p.topics?.length ?? 0) > 0)

  return (
    <div className="cmp-page">
      <div className="cmp-inner">
        <div className="cmp-topbar">
          <button className="cmp-back" onClick={() => navigate(-1)}>← Back</button>
          <button className="cmp-restart" onClick={handleClear}>Clear &amp; start over</button>
        </div>

        <div className="cmp-title-block">
          <h1 className="cmp-title">Project Comparison</h1>
          <p className="cmp-subtitle">Side-by-side across {projects.length} project{projects.length !== 1 ? 's' : ''}</p>
        </div>

        <div className="cmp-table-wrap">
          <table className="cmp-table">
            <thead>
              <tr>
                <th className="cmp-th-label" />
                {projects.map(p => {
                  const label = healthLabel(p.lastUpdated)
                  return (
                    <th key={p.id} className="cmp-th">
                      <div className="cmp-col-header">
                        <span className="cmp-col-name">{p.name}</span>
                        <span className="cmp-col-owner">by {p.owner}</span>
                        <span
                          className="cmp-status-badge"
                          style={{ color: HEALTH_COLORS[label], borderColor: `${HEALTH_COLORS[label]}55` }}
                        >
                          {label.toUpperCase()}
                        </span>
                      </div>
                    </th>
                  )
                })}
              </tr>
            </thead>
            <tbody>
              {/* Health Score */}
              <tr>
                <td className="cmp-row-label">
                  <span className="cmp-label-title">Health Score</span>
                  <span className="cmp-label-sub">Out of 100</span>
                </td>
                {projects.map(p => {
                  const s = scores[p.id]
                  const isBest = topScore.has(p.id)
                  return (
                    <td key={p.id} className={`cmp-cell${isBest ? ' cmp-cell--highlight' : ''}`}>
                      <div className="cmp-score-row">
                        <span className="cmp-score-num">{s}</span>
                        {isBest && <WinTag label="BEST" />}
                      </div>
                      <div className="cmp-bar-track">
                        <div className="cmp-bar-fill" style={{ width: `${s}%` }} />
                      </div>
                    </td>
                  )
                })}
              </tr>

              {/* Stars */}
              <tr>
                <td className="cmp-row-label">
                  <span className="cmp-label-title">Stars</span>
                  <span className="cmp-label-sub">GitHub stars</span>
                </td>
                {projects.map(p => (
                  <td key={p.id} className="cmp-cell">
                    {fmt(p.stars)}
                    {topStars.has(p.id) && <WinTag label="MOST" />}
                  </td>
                ))}
              </tr>

              {/* Forks */}
              <tr>
                <td className="cmp-row-label">
                  <span className="cmp-label-title">Forks</span>
                  <span className="cmp-label-sub">Community contributions</span>
                </td>
                {projects.map(p => (
                  <td key={p.id} className="cmp-cell">
                    {fmt(p.forks)}
                    {topForks.has(p.id) && <WinTag label="MOST" />}
                  </td>
                ))}
              </tr>

              {/* Open Issues */}
              {hasIssues && (
                <tr>
                  <td className="cmp-row-label">
                    <span className="cmp-label-title">Open Issues</span>
                    <span className="cmp-label-sub">Fewer is better</span>
                  </td>
                  {projects.map(p => (
                    <td key={p.id} className="cmp-cell">
                      {p.openIssues !== undefined ? fmt(p.openIssues) : '—'}
                      {fewestIssues.has(p.id) && p.openIssues !== undefined && <WinTag label="FEWEST" />}
                    </td>
                  ))}
                </tr>
              )}

              {/* Last Updated */}
              <tr>
                <td className="cmp-row-label">
                  <span className="cmp-label-title">Last Updated</span>
                  <span className="cmp-label-sub">Most recent activity</span>
                </td>
                {projects.map(p => (
                  <td key={p.id} className="cmp-cell">
                    {p.lastUpdated
                      ? new Date(p.lastUpdated).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })
                      : '—'}
                    {newest.has(p.id) && <WinTag label="NEWEST" />}
                  </td>
                ))}
              </tr>

              {/* Language */}
              <tr>
                <td className="cmp-row-label">
                  <span className="cmp-label-title">Language</span>
                </td>
                {projects.map(p => (
                  <td key={p.id} className="cmp-cell">{p.language || '—'}</td>
                ))}
              </tr>

              {/* License */}
              <tr>
                <td className="cmp-row-label">
                  <span className="cmp-label-title">License</span>
                </td>
                {projects.map(p => (
                  <td key={p.id} className="cmp-cell">{p.license || '—'}</td>
                ))}
              </tr>

              {/* Topics */}
              {hasTopics && (
                <tr>
                  <td className="cmp-row-label">
                    <span className="cmp-label-title">Topics</span>
                  </td>
                  {projects.map(p => (
                    <td key={p.id} className="cmp-cell">
                      {(p.topics?.length ?? 0) > 0 ? (
                        <div className="cmp-topics">
                          {p.topics.slice(0, 5).map(t => (
                            <span key={t} className="cmp-topic-chip">{t}</span>
                          ))}
                        </div>
                      ) : '—'}
                    </td>
                  ))}
                </tr>
              )}

              {/* Repository */}
              <tr>
                <td className="cmp-row-label">
                  <span className="cmp-label-title">Repository</span>
                </td>
                {projects.map(p => (
                  <td key={p.id} className="cmp-cell">
                    <a href={p.githubUrl} target="_blank" rel="noreferrer" className="cmp-gh-link">
                      View on GitHub ↗
                    </a>
                  </td>
                ))}
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      {/* Bottom compare bar — lets user remove projects without leaving */}
      <div className="cmp-footer-bar">
        <span className="cmp-footer-label">COMPARE</span>
        <div className="cmp-footer-chips">
          {projects.map(p => (
            <span key={p.id} className="cmp-footer-chip">
              {p.name}
              <button
                className="cmp-footer-remove"
                onClick={() => handleRemove(p.id)}
                aria-label={`Remove ${p.name}`}
              >×</button>
            </span>
          ))}
        </div>
        <button className="cmp-footer-clear" onClick={handleClear}>Clear</button>
        <span className="cmp-footer-count">Compare {projects.length}</span>
      </div>
    </div>
  )
}
