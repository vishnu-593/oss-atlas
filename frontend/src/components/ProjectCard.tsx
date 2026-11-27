import { Link } from 'react-router-dom'
import type { Project } from '../types/project'
import './ProjectCard.css'

function fmt(n: number): string {
  if (n >= 1000) return `${(n / 1000).toFixed(1)}k`
  return String(n)
}

type HealthStatus = 'active' | 'healthy' | 'slow' | 'inactive'

function getHealthStatus(lastUpdated: string): HealthStatus | null {
  if (!lastUpdated) return null
  const months = (Date.now() - new Date(lastUpdated).getTime()) / (1000 * 60 * 60 * 24 * 30)
  if (months <= 3)  return 'active'
  if (months <= 12) return 'healthy'
  if (months <= 24) return 'slow'
  return 'inactive'
}

const HEALTH_LABEL: Record<HealthStatus, string> = {
  active:   'Active',
  healthy:  'Healthy',
  slow:     'Slow',
  inactive: 'Inactive',
}

interface Props {
  project: Project
  badge?: 'indexed' | 'discovered'
}

export function ProjectCard({ project, badge }: Props) {
  const isExternal = String(project.id).startsWith('http')
  const health = getHealthStatus(project.lastUpdated)

  const inner = (
    <>
      <div className="card-header">
        <span className="card-name">{project.name}</span>
        <div className="card-badges">
          {health && (
            <span className={`card-badge badge-health badge-health-${health}`}>
              {HEALTH_LABEL[health]}
            </span>
          )}
          {badge && (
            <span className={`card-badge badge-${badge}`}>
              {badge === 'indexed' ? 'Indexed' : 'Discovered'}
            </span>
          )}
        </div>
      </div>
      <p className="card-desc">{project.description}</p>
      <div className="card-meta">
        {project.category && <span className="card-tag tag-category">{project.category}</span>}
        <span className="card-tag tag-licence">{project.license}</span>
        <span className="card-lang">{project.language}</span>
      </div>
      {(project.topics?.length ?? 0) > 0 && (
        <div className="card-topics">
          {project.topics.slice(0, 4).map(t => (
            <span key={t} className="topic-chip">{t}</span>
          ))}
          {project.topics.length > 4 && (
            <span className="topic-more">+{project.topics.length - 4}</span>
          )}
        </div>
      )}
      <div className="card-stats">
        <span className="card-stat">
          <svg viewBox="0 0 14 14" fill="none" aria-hidden="true">
            <path d="M7 1l1.6 3.3 3.6.5-2.6 2.6.6 3.6L7 9.3l-3.2 1.7.6-3.6L2 4.8l3.6-.5L7 1z"
              stroke="currentColor" strokeWidth="1.2" strokeLinejoin="round"/>
          </svg>
          {fmt(project.stars)}
        </span>
        <span className="card-stat">
          <svg viewBox="0 0 14 14" fill="none" aria-hidden="true">
            <circle cx="4" cy="3" r="1.5" stroke="currentColor" strokeWidth="1.2"/>
            <circle cx="10" cy="3" r="1.5" stroke="currentColor" strokeWidth="1.2"/>
            <circle cx="7" cy="11" r="1.5" stroke="currentColor" strokeWidth="1.2"/>
            <path d="M4 4.5v2a3 3 0 003 3M10 4.5v2a3 3 0 01-3 3"
              stroke="currentColor" strokeWidth="1.2" strokeLinecap="round"/>
          </svg>
          {fmt(project.forks)}
        </span>
        {project.openIssues !== undefined && (
          <span className="card-stat">
            <svg viewBox="0 0 14 14" fill="none" aria-hidden="true">
              <circle cx="7" cy="7" r="5.5" stroke="currentColor" strokeWidth="1.2"/>
              <path d="M7 4.5v3M7 9.5v.5" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round"/>
            </svg>
            {fmt(project.openIssues)}
          </span>
        )}
      </div>
    </>
  )

  if (isExternal) {
    return (
      <a href={project.githubUrl} target="_blank" rel="noreferrer" className="project-card">
        {inner}
      </a>
    )
  }

  return (
    <Link to={`/projects/${project.id}`} className="project-card">
      {inner}
    </Link>
  )
}
