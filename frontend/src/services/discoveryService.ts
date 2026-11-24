import type { DiscoveryRequest, DiscoveryResult, Project } from '../types/project'
import { API_URLS } from './apiClient'
import { mockProjects } from '../data/mockProjects'

interface BackendProject {
  name: string
  description: string
  githubUrl: string
  owner: string
  language: string
  license: string
  stars: number
  forks: number
  topics: string[] | null
  lastUpdated: string
}

interface BackendSearchResponse {
  query: string
  totalFound: number
  projects: BackendProject[]
}

function toProject(p: BackendProject, index: number): Project {
  return {
    id: p.githubUrl ?? String(index),
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
  }
}

export async function discoverProjects(request: DiscoveryRequest): Promise<DiscoveryResult> {
  try {
    const res = await fetch(`${API_URLS.DISCOVERY_BASE}/api/discovery/search`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
      signal: AbortSignal.timeout(8000),
    })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const data = await res.json() as BackendSearchResponse
    const discovered = (data.projects ?? []).map(toProject)
    return { query: data.query, totalFound: data.totalFound, indexed: [], discovered }
  } catch {
    const { query, language, topic } = request
    const q = query.toLowerCase()
    const matches = mockProjects.filter(p => {
      const textMatch =
        p.name.toLowerCase().includes(q) ||
        p.description.toLowerCase().includes(q) ||
        p.topics.some(t => t.includes(q))
      const langMatch = !language || p.language.toLowerCase() === language.toLowerCase()
      const topicMatch = !topic || p.topics.some(t => t.toLowerCase().includes(topic.toLowerCase()))
      return textMatch && langMatch && topicMatch
    })
    const indexed = matches.filter(p => p.isIndexed)
    const discovered = matches.filter(p => !p.isIndexed)
    return { indexed, discovered, query, totalFound: matches.length }
  }
}
