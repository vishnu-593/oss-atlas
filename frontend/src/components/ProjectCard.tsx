import { Link } from 'react-router-dom'
import type { Project } from '../types/project'
import './ProjectCard.css'

function fmt(n: number): string {
  if (n >= 1000) return `${(n / 1000).toFixed(1)}k`
  return String(n)
}

interface Props {
  project: Project
  badge?: 'indexed' | 'discovered'
  compareSelected?: boolean
  onToggleCompare?: (project: Project) => void
}

export function ProjectCard({ project, badge, compareSelected, onToggleCompare }: Props) {
  const isExternal = String(project.id).startsWith('http')

  const inner = (
    <>
      <div className="card-header">
        <span className="card-name">{project.name}</span>
        {badge && (
          <span className={`card-badge badge-${badge}`}>
            {badge === 'indexed' ? 'Indexed' : 'Discovered'}
          </span>
        )}
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
        {onToggleCompare && (
          <button
            className={`card-compare-btn${compareSelected ? ' card-compare-btn--on' : ''}`}
            onClick={e => { e.preventDefault(); e.stopPropagation(); onToggleCompare(project) }}
            title={compareSelected ? 'Remove from comparison' : 'Add to comparison'}
          >
            {compareSelected ? '✓ Comparing' : '+ Compare'}
          </button>
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
