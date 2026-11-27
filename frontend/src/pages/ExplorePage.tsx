import { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import type { ProjectFilters, Project } from '../types/project'
import { useProjects } from '../hooks/useProjects'
import { useBookmarks } from '../hooks/useBookmarks'
import { ProjectCard } from '../components/ProjectCard'
import { FilterPanel } from '../components/FilterPanel'
import { Pagination } from '../components/Pagination'
import { LoadingState } from '../components/LoadingState'
import { ErrorState } from '../components/ErrorState'
import { EmptyState } from '../components/EmptyState'
import { StatsPanel } from '../components/StatsPanel'
import { ComparisonDrawer } from '../components/ComparisonDrawer'
import './ExplorePage.css'

export function ExplorePage() {
  const [searchParams] = useSearchParams()
  const [filters, setFilters] = useState<Partial<ProjectFilters>>({
    search: searchParams.get('q') ?? '',
  })
  const [page, setPage] = useState(0)
  const { data, loading, error, reload } = useProjects(filters, page)
  const { toggle: toggleBookmark, isBookmarked } = useBookmarks()
  const [compareMap, setCompareMap] = useState<Map<string, Project>>(new Map())

  useEffect(() => {
    const q = searchParams.get('q')
    if (q) setFilters(f => ({ ...f, search: q }))
  }, [searchParams])

  function handleFiltersChange(next: Partial<ProjectFilters>) {
    setFilters(next)
    setPage(0)
  }

  function handleSearch(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const q = (e.currentTarget.elements.namedItem('q') as HTMLInputElement).value
    handleFiltersChange({ ...filters, search: q })
  }

  function handleToggleCompare(project: Project) {
    setCompareMap(prev => {
      const next = new Map(prev)
      if (next.has(project.id)) {
        next.delete(project.id)
      } else if (next.size < 3) {
        next.set(project.id, project)
      }
      return next
    })
  }

  const compareProjects = Array.from(compareMap.values())

  return (
    <div className="explore-page" style={{ paddingBottom: compareProjects.length > 0 ? '14rem' : undefined }}>
      <div className="explore-header">
        <h1>Browse Catalogue</h1>
        <p>Explore {data?.totalElements ?? '…'} open-source projects</p>
        <form className="search-form" onSubmit={handleSearch}>
          <input
            name="q"
            type="search"
            value={filters.search ?? ''}
            onChange={e => handleFiltersChange({ ...filters, search: e.target.value })}
            className="search-input"
            placeholder="Search projects…"
            aria-label="Search projects"
          />
          <button type="submit" className="search-btn">Search</button>
        </form>
      </div>

      <StatsPanel />

      <div className="explore-body">
        <FilterPanel filters={filters} onChange={handleFiltersChange} />
        <div className="explore-results">
          {loading && <LoadingState />}
          {error && !loading && <ErrorState message={error} onRetry={reload} />}
          {!loading && !error && data && (
            <>
              {data.content.length === 0 ? (
                <EmptyState message="No projects match your filters." />
              ) : (
                <div className="projects-grid">
                  {data.content.map(p => (
                    <ProjectCard
                      key={p.id}
                      project={p}
                      bookmarked={isBookmarked(p.id)}
                      onToggleBookmark={toggleBookmark}
                      compareSelected={compareMap.has(p.id)}
                      onToggleCompare={handleToggleCompare}
                    />
                  ))}
                </div>
              )}
              <Pagination page={data.page} totalPages={data.totalPages} onPageChange={setPage} />
            </>
          )}
        </div>
      </div>

      <ComparisonDrawer
        projects={compareProjects}
        onRemove={id => setCompareMap(prev => { const m = new Map(prev); m.delete(id); return m })}
        onClear={() => setCompareMap(new Map())}
      />
    </div>
  )
}
