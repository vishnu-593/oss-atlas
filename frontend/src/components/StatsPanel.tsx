import { useEffect, useState } from 'react'
import { getCatalogueStats } from '../services/catalogueService'
import type { CatalogueStats } from '../types/project'
import './StatsPanel.css'

const HEALTH_COLORS: Record<string, string> = {
  Active:   '#4ade80',
  Healthy:  '#86efac',
  Slow:     '#fbbf24',
  Inactive: '#f87171',
}

export function StatsPanel() {
  const [stats, setStats] = useState<CatalogueStats | null>(null)

  useEffect(() => {
    getCatalogueStats().then(setStats).catch(() => {})
  }, [])

  if (!stats) return null

  return (
    <div className="stats-panel">
      <div className="stats-cell">
        <span className="stats-value">{stats.totalProjects.toLocaleString()}</span>
        <span className="stats-unit">projects</span>
      </div>

      {stats.topLanguages.length > 0 && (
        <>
          <div className="stats-divider" />
          <div className="stats-cell stats-cell--col">
            <span className="stats-section-label">Top languages</span>
            <div className="stats-chips">
              {stats.topLanguages.slice(0, 6).map(l => (
                <span key={l.name} className="stats-chip">
                  {l.name} <em>{l.count}</em>
                </span>
              ))}
            </div>
          </div>
        </>
      )}

      {stats.licenseDistribution.length > 0 && (
        <>
          <div className="stats-divider" />
          <div className="stats-cell stats-cell--col">
            <span className="stats-section-label">Licenses</span>
            <div className="stats-chips">
              {stats.licenseDistribution.slice(0, 4).map(l => (
                <span key={l.name} className="stats-chip">
                  {l.name} <em>{l.count}</em>
                </span>
              ))}
            </div>
          </div>
        </>
      )}

      {stats.healthDistribution.length > 0 && (
        <>
          <div className="stats-divider" />
          <div className="stats-cell stats-cell--col">
            <span className="stats-section-label">Activity</span>
            <div className="stats-health">
              {stats.healthDistribution.map(h => (
                <span key={h.name} className="stats-health-item">
                  <span
                    className="stats-health-dot"
                    style={{ background: HEALTH_COLORS[h.name] ?? 'var(--text-muted)' }}
                  />
                  {h.name} {h.count}
                </span>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  )
}
