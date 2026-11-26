import type { Project, ProjectFilters, PagedResponse } from '../types/project'
import { fetchJson, API_URLS } from './apiClient'

/**
 * Raw record as returned by the catalogue API. Accepts both the current
 * shape (githubUrl/topics/forks) and the older one (repositoryUrl/tags),
 * so this keeps working whether or not the backend is on the latest build.
 */
interface RawProject {
  id: number | string
  name?: string
  description?: string
  githubUrl?: string
  repositoryUrl?: string
  owner?: string
  language?: string
  license?: string
  stars?: number
  forks?: number
  topics?: string[] | null
  tags?: string[] | null
  lastUpdated?: string
  updatedAt?: string
  category?: string
  homepage?: string
}

function ownerFromUrl(url: string): string {
  const m = url.match(/github\.com\/([^/]+)/i)
  return m ? m[1] : ''
}

function toProject(p: RawProject): Project {
  const githubUrl = p.githubUrl ?? p.repositoryUrl ?? ''
  return {
    id: String(p.id),
    name: p.name ?? '',
    description: p.description ?? '',
    githubUrl,
    owner: p.owner ?? ownerFromUrl(githubUrl),
    language: p.language ?? '',
    license: (p.license as Project['license']) ?? 'Other',
    stars: p.stars ?? 0,
    forks: p.forks ?? 0,
    topics: p.topics ?? p.tags ?? [],
    lastUpdated: p.lastUpdated ?? p.updatedAt ?? '',
    category: p.category as Project['category'] | undefined,
    homepage: p.homepage,
  }
}

function applyFilters(projects: Project[], filters: Partial<ProjectFilters>): Project[] {
  return projects.filter(p => {
    if (filters.search) {
      const q = filters.search.toLowerCase()
      const hit =
        p.name.toLowerCase().includes(q) ||
        p.description.toLowerCase().includes(q) ||
        p.language.toLowerCase().includes(q) ||
        p.topics.some(t => t.toLowerCase().includes(q))
      if (!hit) return false
    }
    if (filters.language && p.language.toLowerCase() !== filters.language.toLowerCase()) return false
    if (filters.license && p.license !== filters.license) return false
    if (filters.topic) {
      const t = filters.topic.toLowerCase()
      if (!p.topics.some(tag => tag.toLowerCase().includes(t))) return false
    }
    return true
  })
}

function paginate(projects: Project[], page: number, size: number): PagedResponse<Project> {
  const start = page * size
  return {
    content: projects.slice(start, start + size),
    totalElements: projects.length,
    totalPages: Math.max(1, Math.ceil(projects.length / size)),
    page,
    size,
  }
}

/**
 * The catalogue list endpoint ignores search/filter params and caps page
 * size, so we pull the full catalogue and filter in the browser. Cached
 * briefly (not forever) so projects imported during a session still show up.
 * Reads githubUrl ?? repositoryUrl and topics ?? tags, so it keeps working
 * unchanged once the backend ships the corrected /search.
 */
let cache: { at: number; data: Project[] } | null = null
const CACHE_TTL_MS = 15_000

async function fetchAllProjects(): Promise<Project[]> {
  if (cache && Date.now() - cache.at < CACHE_TTL_MS) return cache.data
  const size = 100
  const first = await fetchJson<PagedResponse<RawProject>>(
    `${API_URLS.CATALOGUE_BASE}/api/projects?page=0&size=${size}`,
  )
  const raw = [...first.content]
  const totalPages = Math.min(first.totalPages ?? 1, 20)
  if (totalPages > 1) {
    const rest = await Promise.all(
      Array.from({ length: totalPages - 1 }, (_, i) =>
        fetchJson<PagedResponse<RawProject>>(
          `${API_URLS.CATALOGUE_BASE}/api/projects?page=${i + 1}&size=${size}`,
        ),
      ),
    )
    rest.forEach(r => raw.push(...r.content))
  }
  const data = raw.map(toProject)
  cache = { at: Date.now(), data }
  return data
}

export async function getProjects(
  filters: Partial<ProjectFilters> = {},
  page = 0,
  size = 12,
): Promise<PagedResponse<Project>> {
  const all = await fetchAllProjects()
  return paginate(applyFilters(all, filters), page, size)
}

export async function getProjectById(id: string): Promise<Project> {
  const raw = await fetchJson<RawProject>(`${API_URLS.CATALOGUE_BASE}/api/projects/${id}`)
  return toProject(raw)
}

export async function getProjectReadme(id: string): Promise<string | null> {
  try {
    const res = await fetch(`${API_URLS.CATALOGUE_BASE}/api/projects/${id}/readme`, {
      signal: AbortSignal.timeout(5000),
    })
    if (!res.ok) return null
    return res.text()
  } catch {
    return null
  }
}
