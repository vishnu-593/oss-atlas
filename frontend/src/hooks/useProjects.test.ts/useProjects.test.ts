import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, waitFor, act } from '@testing-library/react'
import { useProjects } from './useProjects'
import { getProjects } from '../services/catalogueService'
import type { PagedResponse, Project } from '../types/project'

vi.mock('../services/catalogueService', () => ({
  getProjects: vi.fn(),
}))

const mockedGetProjects = vi.mocked(getProjects)

const project: Project = {
  id: 'oss-atlas',
  name: 'OSS Atlas',
  description: 'Discover open source projects.',
  githubUrl: 'https://github.com/vishnu-593/oss-atlas',
  owner: 'vishnu-593',
  language: 'TypeScript',
  license: 'MIT',
  stars: 1200,
  forks: 45,
  topics: ['react', 'typescript'],
  lastUpdated: '2026-11-20',
}

const paged: PagedResponse<Project> = {
  content: [project],
  totalElements: 1,
  totalPages: 1,
  page: 0,
  size: 12,
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('useProjects', () => {
  it('starts loading and then returns the fetched page', async () => {
    mockedGetProjects.mockResolvedValue(paged)
    const { result } = renderHook(() => useProjects({}, 0))
    expect(result.current.loading).toBe(true)
    expect(result.current.data).toBeNull()
    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.data).toEqual(paged)
    expect(result.current.error).toBeNull()
  })

  it('passes the filters and page to the service', async () => {
    mockedGetProjects.mockResolvedValue(paged)
    const { result } = renderHook(() => useProjects({ search: 'react' }, 2))
    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(mockedGetProjects).toHaveBeenCalledWith({ search: 'react' }, 2)
  })

  it('reports an error when the fetch fails', async () => {
    mockedGetProjects.mockRejectedValue(new Error('Network error'))
    const { result } = renderHook(() => useProjects({}, 0))
    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.data).toBeNull()
    expect(result.current.error).toBe('Error: Network error')
  })

  it('reloads the page when reload is called', async () => {
    mockedGetProjects.mockResolvedValue(paged)
    const { result } = renderHook(() => useProjects({}, 0))
    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(mockedGetProjects).toHaveBeenCalledTimes(1)
    await act(async () => { await result.current.reload() })
    expect(mockedGetProjects).toHaveBeenCalledTimes(2)
  })
})
