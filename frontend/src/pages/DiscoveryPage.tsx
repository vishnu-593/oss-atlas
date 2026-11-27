import { useState } from 'react'
import { useDiscovery } from '../hooks/useDiscovery'
import { useBookmarks } from '../hooks/useBookmarks'
import { ProjectCard } from '../components/ProjectCard'
import { LoadingState } from '../components/LoadingState'
import { ErrorState } from '../components/ErrorState'
import { EmptyState } from '../components/EmptyState'
import './DiscoveryPage.css'

const LANGUAGES = ['TypeScript', 'JavaScript', 'Python', 'Java', 'Go', 'Rust', 'C', 'C++']
const LICENSES = ['MIT', 'Apache-2.0', 'GPL-3.0', 'BSD-3-Clause', 'MPL-2.0', 'ISC']

export function DiscoveryPage() {
  const [query, setQuery] = useState('')
  const [language, setLanguage] = useState('')
  const [topic, setTopic] = useState('')
  const [minStars, setMinStars] = useState('')
  const [license, setLicense] = useState('')
  const [limit, setLimit] = useState(10)
  const { result, loading, error, search } = useDiscovery()
  const { toggle, isBookmarked } = useBookmarks()

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    void search({
      query,
      language: language || undefined,
      topic: topic || undefined,
      minStars: minStars ? Number(minStars) : undefined,
      license: license || undefined,
      limit,
    })
  }

  return (
    <div className="discovery-page">
      <div className="discovery-header">
        <div className="discovery-title-row">
          <svg className="github-mark" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
            <path fillRule="evenodd" clipRule="evenodd"
              d="M12 0C5.37 0 0 5.37 0 12c0 5.303 3.438 9.8 8.205 11.387.6.113.82-.258.82-.577
                 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61-.546-1.387-1.333-1.756
                 -1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237
                 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305
                 -5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524
                 .117-3.176 0 0 1.008-.322 3.301 1.23A11.509 11.509 0 0112 5.803c1.02.005
                 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118
                 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823
                 1.102.823 2.222 0 1.606-.015 2.898-.015 3.293 0 .319.216.694.825.576C20.565
                 21.795 24 17.298 24 12c0-6.63-5.37-12-12-12z"/>
          </svg>
          <h1>GitHub Discovery</h1>
        </div>
        <p>Search GitHub to find open-source projects — see which ones are already indexed in OSS Atlas and which are newly discovered.</p>

        <form className="discovery-form" onSubmit={handleSubmit}>
          <div className="discovery-inputs">
            <input
              type="search"
              className="discovery-input"
              value={query}
              onChange={e => setQuery(e.target.value)}
              placeholder="Keyword, e.g. machine learning…"
              aria-label="Search keyword"
              required
            />
            <select
              className="discovery-select"
              value={language}
              onChange={e => setLanguage(e.target.value)}
              aria-label="Filter by language"
            >
              <option value="">Any language</option>
              {LANGUAGES.map(l => <option key={l} value={l}>{l}</option>)}
            </select>
            <input
              type="text"
              className="discovery-input discovery-input-sm"
              value={topic}
              onChange={e => setTopic(e.target.value)}
              placeholder="Topic, e.g. database"
              aria-label="Filter by topic"
            />
            <input
              type="number"
              className="discovery-input discovery-input-sm"
              value={minStars}
              onChange={e => setMinStars(e.target.value)}
              placeholder="Min stars, e.g. 500"
              aria-label="Minimum stars"
              min={0}
            />
            <select
              className="discovery-select"
              value={license}
              onChange={e => setLicense(e.target.value)}
              aria-label="Filter by license"
            >
              <option value="">Any license</option>
              {LICENSES.map(l => <option key={l} value={l}>{l}</option>)}
            </select>
          </div>
          <div className="discovery-row">
            <select
              className="discovery-select"
              value={limit}
              onChange={e => setLimit(Number(e.target.value))}
              aria-label="Number of results"
            >
              <option value={10}>10 results</option>
              <option value={20}>20 results</option>
              <option value={30}>30 results</option>
              <option value={50}>50 results</option>
            </select>
            <button type="submit" className="discovery-btn" disabled={loading || !query.trim()}>
              {loading ? 'Searching…' : 'Discover'}
            </button>
          </div>
        </form>
      </div>

      {loading && <LoadingState message="Searching GitHub…" />}
      {error && !loading && <ErrorState message={error} />}
      {importError && <div className="import-error">{importError}</div>}

      {result && !loading && (
        <div className="discovery-results">
          <div className="discovery-summary">
            Found {result.totalFound} project{result.totalFound !== 1 ? 's' : ''} matching "{result.query}"
            {language && <> · {language}</>}
            {topic && <> · #{topic}</>}
            {minStars && <> · ★ {minStars}+</>}
            {license && <> · {license}</>}
          </div>

          {result.indexed.length > 0 && (
            <section className="result-section">
              <h2 className="section-title">
                <span className="section-badge badge-indexed">✓ Already Indexed</span>
                {result.indexed.length} project{result.indexed.length !== 1 ? 's' : ''}
              </h2>
              <div className="discovery-grid">
                {result.indexed.map(p => (
                  <ProjectCard
                    key={p.id}
                    project={p}
                    badge="indexed"
                    bookmarked={isBookmarked(p.id)}
                    onToggleBookmark={toggle}
                  />
                ))}
              </div>
            </section>
          )}

          {result.discovered.length > 0 && (
            <section className="result-section">
              <h2 className="section-title">
                <span className="section-badge badge-discovered">✦ Newly Discovered</span>
                {result.discovered.length} project{result.discovered.length !== 1 ? 's' : ''}
              </h2>
              <div className="discovery-grid">
                {result.discovered.map(p => (
                  <ProjectCard
                    key={p.id}
                    project={p}
                    badge="discovered"
                    bookmarked={isBookmarked(p.id)}
                    onToggleBookmark={toggle}
                  />
                ))}
              </div>
            </section>
          )}

          {result.indexed.length === 0 && result.discovered.length === 0 && (
            <EmptyState message={`No results found for "${result.query}".`} />
          )}
        </div>
      )}
    </div>
  )
}
