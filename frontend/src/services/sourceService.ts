import type { Project } from '../types/project'
import { fetchJson, API_URLS } from './apiClient'
import { mockProjects } from '../data/mockProjects'

export async function getGitHubStats(owner: string, repo: string): Promise<Partial<Project>> {
  try {
    return await fetchJson<Partial<Project>>(
      `${API_URLS.SOURCE_BASE}/api/github/${owner}/${repo}`,
    )
  } catch {
    const project = mockProjects.find(p => p.owner === owner && p.name.toLowerCase() === repo.toLowerCase())
    return project ?? {}
  }
}
