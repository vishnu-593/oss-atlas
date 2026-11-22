import type { DiscoveryRequest, DiscoveryResult } from '../types/project'
import { API_URLS } from './apiClient'
import { mockProjects } from '../data/mockProjects'

export async function discoverProjects(request: DiscoveryRequest): Promise<DiscoveryResult> {
  try {
    const res = await fetch(`${API_URLS.DISCOVERY_BASE}/api/discovery/search`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
      signal: AbortSignal.timeout(8000),
    })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    return res.json() as Promise<DiscoveryResult>
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
