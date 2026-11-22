import { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import type { ProjectFilters } from '../types/project'
import { useProjects } from '../hooks/useProjects'
import { ProjectCard } from '../components/ProjectCard'
import { FilterPanel } from '../components/FilterPanel'
import { Pagination } from '../components/Pagination'
import { LoadingState } from '../components/LoadingState'
import { ErrorState } from '../components/ErrorState'
import { EmptyState } from '../components/EmptyState'
import './ExplorePage.css'

export function ExplorePage() {
  const [searchParams] = useSearchParams()
  const [filters, setFilters] = useState<Partial<ProjectFilters>>({
    search: searchParams.get('q') ?? '',
  })
  const [page, setPage] = useState(0)
  const { data, loading, error, reload } = useProjects(filters, page)

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

  return (
    <div className="explore-page">
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
                  {data.content.map(p => <ProjectCard key={p.id} project={p} />)}
                </div>
              )}
              <Pagination page={data.page} totalPages={data.totalPages} onPageChange={setPage} />
            </>
          )}
        </div>
      </div>
    </div>
  )
}
