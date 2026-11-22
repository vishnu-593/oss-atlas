import type { ProjectFilters, License } from '../types/project'
import './FilterPanel.css'

const LICENSES: License[] = [
  'MIT', 'Apache-2.0', 'GPL-3.0', 'BSD-3-Clause', 'MPL-2.0', 'LGPL-2.1', 'ISC', 'Other',
]

const LANGUAGES = ['TypeScript', 'JavaScript', 'Python', 'Java', 'Go', 'Rust', 'C', 'C++']

interface Props {
  filters: Partial<ProjectFilters>
  onChange: (filters: Partial<ProjectFilters>) => void
}

export function FilterPanel({ filters, onChange }: Props) {
  function set(key: keyof ProjectFilters, value: string) {
    onChange({ ...filters, [key]: value })
  }

  return (
    <aside className="filter-panel">
      <h3 className="filter-title">Filters</h3>

      <div className="filter-group">
        <label className="filter-label" htmlFor="filter-lang">Language</label>
        <select
          id="filter-lang"
          className="filter-select"
          value={filters.language ?? ''}
          onChange={e => set('language', e.target.value)}
        >
          <option value="">All languages</option>
          {LANGUAGES.map(l => <option key={l} value={l}>{l}</option>)}
        </select>
      </div>

      <div className="filter-group">
        <label className="filter-label" htmlFor="filter-license">Licence</label>
        <select
          id="filter-license"
          className="filter-select"
          value={filters.license ?? ''}
          onChange={e => set('license', e.target.value)}
        >
          <option value="">All licences</option>
          {LICENSES.map(l => <option key={l} value={l}>{l}</option>)}
        </select>
      </div>

      <div className="filter-group">
        <label className="filter-label" htmlFor="filter-topic">Topic</label>
        <input
          id="filter-topic"
          type="text"
          className="filter-select"
          value={filters.topic ?? ''}
          onChange={e => set('topic', e.target.value)}
          placeholder="e.g. database, security"
        />
      </div>

      <button
        className="filter-reset"
        onClick={() => onChange({ search: filters.search ?? '' })}
      >
        Reset filters
      </button>
    </aside>
  )
}
