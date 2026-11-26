import type { Project, ProjectFilters, PagedResponse } from '../types/project'
import { fetchJson, API_URLS } from './apiClient'

interface RawProject {
  id: number | string
  name?: string
  description?: string
  githubUrl?: string
  owner?: string
  language?: string
  license?: string
  stars?: number
  forks?: number
  topics?: string[] | null
  lastUpdated?: string
  category?: string
  homepage?: string
}

function toProject(p: RawProject): Project {
  return {
    id: String(p.id),
    name: p.name ?? '',
    description: p.description ?? '',
    githubUrl: p.githubUrl ?? '',
    owner: p.owner ?? '',
    language: p.language ?? '',
    license: (p.license as Project['license']) ?? 'Other',
    stars: p.stars ?? 0,
    forks: p.forks ?? 0,
    topics: p.topics ?? [],
    lastUpdated: p.lastUpdated ?? '',
    category: p.category as Project['category'] | undefined,
    homepage: p.homepage,
  }
}

export async function getProjects(
  filters: Partial<ProjectFilters> = {},
  page = 0,
  size = 12,
): Promise<PagedResponse<Project>> {
  const hasFilters = filters.search || filters.language || filters.license || filters.topic

  const params = new URLSearchParams()
  params.set('page', String(page))
  params.set('size', String(size))

  let url: string
  if (hasFilters) {
    if (filters.search)   params.set('keyword', filters.search)
    if (filters.language) params.set('language', filters.language)
    if (filters.license)  params.set('license', filters.license)
    if (filters.topic)    params.set('topic', filters.topic)
    url = `${API_URLS.CATALOGUE_BASE}/api/projects/search?${params}`
  } else {
    params.set('sortBy', 'createdAt')
    params.set('direction', 'desc')
    url = `${API_URLS.CATALOGUE_BASE}/api/projects?${params}`
  }

  const raw = await fetchJson<PagedResponse<RawProject>>(url)
  return {
    content: raw.content.map(toProject),
    totalElements: raw.totalElements,
    totalPages: raw.totalPages,
    page: raw.page,
    size: raw.size,
  }
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
