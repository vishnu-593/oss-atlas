export type Category =
  | 'DevTools'
  | 'Database'
  | 'Framework'
  | 'Library'
  | 'Infrastructure'
  | 'Security'
  | 'Monitoring'
  | 'AI/ML'
  | 'Other'

export type License =
  | 'MIT'
  | 'Apache-2.0'
  | 'GPL-3.0'
  | 'BSD-3-Clause'
  | 'MPL-2.0'
  | 'LGPL-2.1'
  | 'ISC'
  | 'Other'

/** Shape returned by backend API */
export interface Project {
  id: string
  name: string
  description: string
  githubUrl: string
  owner: string
  language: string
  license: License
  stars: number
  forks: number
  topics: string[]
  lastUpdated: string
  openIssues?: number
  healthScore?: number
  healthLabel?: string
  /** Optional fields not guaranteed by API contract */
  category?: Category
  isIndexed?: boolean
  homepage?: string
}

export interface ProjectFilters {
  search: string
  language: string
  license: License | ''
  topic: string
}

export interface PagedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}

export interface DiscoveryRequest {
  query: string
  language?: string
  topic?: string
  minStars?: number
  license?: string
  limit?: number
}

export interface DiscoveryResult {
  indexed: Project[]
  discovered: Project[]
  query: string
  totalFound: number
}
