import type { Project, ProjectFilters, PagedResponse } from '../types/project'
import { fetchJson, API_URLS } from './apiClient'
import { mockProjects } from '../data/mockProjects'

function applyFilters(projects: Project[], filters: Partial<ProjectFilters>): Project[] {
  return projects.filter(p => {
    if (filters.search) {
      const q = filters.search.toLowerCase()
      if (!p.name.toLowerCase().includes(q) && !p.description.toLowerCase().includes(q)) return false
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

export async function getProjects(
  filters: Partial<ProjectFilters> = {},
  page = 0,
  size = 12,
): Promise<PagedResponse<Project>> {
  try {
    const params = new URLSearchParams({
      page: String(page),
      size: String(size),
      ...(filters.search ? { search: filters.search } : {}),
      ...(filters.language ? { language: filters.language } : {}),
      ...(filters.license ? { license: filters.license } : {}),
      ...(filters.topic ? { topic: filters.topic } : {}),
    })
    return await fetchJson<PagedResponse<Project>>(`${API_URLS.CATALOGUE_BASE}/api/projects?${params}`)
  } catch {
    const filtered = applyFilters(mockProjects, filters)
    const start = page * size
    return {
      content: filtered.slice(start, start + size),
      totalElements: filtered.length,
      totalPages: Math.ceil(filtered.length / size),
      page,
      size,
    }
  }
}

export async function getProjectById(id: string): Promise<Project> {
  try {
    return await fetchJson<Project>(`${API_URLS.CATALOGUE_BASE}/api/projects/${id}`)
  } catch {
    const project = mockProjects.find(p => p.id === id)
    if (!project) throw new Error(`Project ${id} not found`)
    return project
  }
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
