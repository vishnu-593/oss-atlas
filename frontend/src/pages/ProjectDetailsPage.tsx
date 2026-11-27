import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import type { Project } from '../types/project'
import { getProjectById, getProjectReadme, getProjects } from '../services/catalogueService'
import { LoadingState } from '../components/LoadingState'
import { ErrorState } from '../components/ErrorState'
import { ProjectCard } from '../components/ProjectCard'
import './ProjectDetailsPage.css'

function formatNumber(n: number): string {
  if (n >= 1000) return `${(n / 1000).toFixed(1)}k`
  return String(n)
}

export function ProjectDetailsPage() {
  const { id } = useParams<{ id: string }>()
  const [project, setProject] = useState<Project | null>(null)
  const [readme, setReadme] = useState<string | null>(null)
  const [similar, setSimilar] = useState<Project[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    setLoading(true)
    setError(null)
    setReadme(null)
    setSimilar([])
    getProjectById(id)
      .then(p => {
        setProject(p)
        if (p.language) {
          getProjects({ language: p.language }, 0, 8)
            .then(r => setSimilar(r.content.filter(s => s.id !== id).slice(0, 4)))
            .catch(() => {})
        }
        return getProjectReadme(id)
      })
      .then(setReadme)
      .catch(e => setError(String(e)))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) return <LoadingState />
  if (error || !project) return <ErrorState message={error ?? 'Project not found.'} />

  return (
    <div className="details-page">
      <div className="details-inner">
        <Link to="/explore" className="back-link">← Back to catalogue</Link>

        <div className="details-header">
          <div>
            <h1 className="details-name">{project.name}</h1>
            <p className="details-owner">by {project.owner}</p>
          </div>
          <div className="details-actions">
            <a href={project.githubUrl} target="_blank" rel="noreferrer" className="btn-outline">
              View on GitHub ↗
            </a>
            {project.homepage && (
              <a href={project.homepage} target="_blank" rel="noreferrer" className="btn-outline">
                Homepage ↗
              </a>
            )}
          </div>
        </div>

        <p className="details-desc">{project.description}</p>

        <div className="details-tags">
          {project.category && <span className="tag tag-category">{project.category}</span>}
          <span className="tag tag-licence">{project.license}</span>
          <span className="tag tag-lang">{project.language}</span>
          {project.isIndexed && <span className="tag tag-indexed">✓ Indexed</span>}
        </div>

        <div className="details-stats">
          <div className="stat">
            <span className="stat-value">★ {formatNumber(project.stars)}</span>
            <span className="stat-label">Stars</span>
          </div>
          <div className="stat">
            <span className="stat-value">⑂ {formatNumber(project.forks)}</span>
            <span className="stat-label">Forks</span>
          </div>
          {project.openIssues !== undefined && (
            <div className="stat">
              <span className="stat-value">{formatNumber(project.openIssues)}</span>
              <span className="stat-label">Open Issues</span>
            </div>
          )}
          <div className="stat">
            <span className="stat-value">{project.lastUpdated}</span>
            <span className="stat-label">Last Updated</span>
          </div>
        </div>

        {(project.topics?.length ?? 0) > 0 && (
          <div className="details-topics">
            <h3>Topics</h3>
            <div className="topics-list">
              {project.topics.map(t => (
                <span key={t} className="topic-chip">{t}</span>
              ))}
            </div>
          </div>
        )}

        {readme && (
          <div className="details-readme">
            <h3>README</h3>
            <pre className="readme-content">{readme}</pre>
          </div>
        )}

        {similar.length > 0 && (
          <div className="similar-section">
            <h3 className="similar-title">Similar Projects</h3>
            <div className="similar-grid">
              {similar.map(s => <ProjectCard key={s.id} project={s} />)}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
