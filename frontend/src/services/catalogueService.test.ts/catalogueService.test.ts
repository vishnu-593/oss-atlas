import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { getProjects, getProjectById } from './catalogueService'

const fetchMock = vi.fn()

function jsonResponse(payload: unknown) {
  return { ok: true, json: async () => payload }
}

const rawProject = {
  id: 42,
  name: 'OSS Atlas',
  description: 'Discover open source projects.',
  githubUrl: 'https://github.com/vishnu-593/oss-atlas',
  language: 'TypeScript',
  license: 'MIT',
  stars: 1200,
  forks: 45,
  topics: ['react', 'typescript'],
  lastUpdated: '2026-11-20',
  category: 'DevTools',
}

const pagePayload = {
  content: [rawProject],
  totalElements: 1,
  totalPages: 1,
  number: 0,
  size: 12,
}

beforeEach(() => {
  fetchMock.mockReset()
  vi.stubGlobal('fetch', fetchMock)
})

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('catalogueService', () => {
  it('fetches a page of projects without filters', async () => {
    fetchMock.mockResolvedValue(jsonResponse(pagePayload))
    const page = await getProjects({}, 0)
    expect(fetchMock.mock.calls[0][0]).toBe(
      'http://localhost:8081/api/projects?page=0&size=12&sortBy=createdAt&direction=desc',
    )
    expect(page.content).toHaveLength(1)
    expect(page.content[0].id).toBe('42')
    expect(page.content[0].name).toBe('OSS Atlas')
    expect(page.content[0].owner).toBe('vishnu-593')
    expect(page.totalElements).toBe(1)
    expect(page.page).toBe(0)
  })

  it('builds the search URL from the filters', async () => {
    fetchMock.mockResolvedValue(jsonResponse(pagePayload))
    await getProjects({ search: 'react', language: 'TypeScript', license: 'MIT', topic: 'database' }, 1)
    expect(fetchMock.mock.calls[0][0]).toBe(
      'http://localhost:8081/api/projects/search?page=1&size=12&keyword=react&language=TypeScript&license=MIT&topic=database',
    )
  })

  it('fetches a single project by id', async () => {
    fetchMock.mockResolvedValue(jsonResponse(rawProject))
    const project = await getProjectById('42')
    expect(fetchMock.mock.calls[0][0]).toBe('http://localhost:8081/api/projects/42')
    expect(project.id).toBe('42')
    expect(project.stars).toBe(1200)
    expect(project.topics).toEqual(['react', 'typescript'])
  })

  it('normalises missing fields to safe defaults', async () => {
    fetchMock.mockResolvedValue(jsonResponse({ id: 7 }))
    const project = await getProjectById('7')
    expect(project.id).toBe('7')
    expect(project.name).toBe('')
    expect(project.stars).toBe(0)
    expect(project.topics).toEqual([])
    expect(project.license).toBe('Other')
  })
})
