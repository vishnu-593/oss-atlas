import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useDiscovery } from './useDiscovery'
import { discoverProjects } from '../services/discoveryService'
import type { DiscoveryResult, Project } from '../types/project'

vi.mock('../services/discoveryService', () => ({
  discoverProjects: vi.fn(),
}))

const mockedDiscoverProjects = vi.mocked(discoverProjects)

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

const discoveryResult: DiscoveryResult = {
  indexed: [project],
  discovered: [],
  query: 'react',
  totalFound: 1,
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('useDiscovery', () => {
  it('starts with no result, not loading and no error', () => {
    const { result } = renderHook(() => useDiscovery())
    expect(result.current.result).toBeNull()
    expect(result.current.loading).toBe(false)
    expect(result.current.error).toBeNull()
  })

  it('returns the search result', async () => {
    mockedDiscoverProjects.mockResolvedValue(discoveryResult)
    const { result } = renderHook(() => useDiscovery())
    await act(async () => { await result.current.search({ query: 'react' }) })
    expect(result.current.result).toEqual(discoveryResult)
    expect(result.current.loading).toBe(false)
    expect(result.current.error).toBeNull()
    expect(mockedDiscoverProjects).toHaveBeenCalledWith({ query: 'react' })
  })

  it('does not search when the query is empty', async () => {
    mockedDiscoverProjects.mockResolvedValue(discoveryResult)
    const { result } = renderHook(() => useDiscovery())
    await act(async () => { await result.current.search({ query: '   ' }) })
    expect(mockedDiscoverProjects).not.toHaveBeenCalled()
    expect(result.current.result).toBeNull()
  })

  it('reports an error when the search fails', async () => {
    mockedDiscoverProjects.mockRejectedValue(new Error('Discovery failed'))
    const { result } = renderHook(() => useDiscovery())
    await act(async () => { await result.current.search({ query: 'react' }) })
    expect(result.current.result).toBeNull()
    expect(result.current.loading).toBe(false)
    expect(result.current.error).toBe('Error: Discovery failed')
  })

  it('returns an empty result when nothing matches', async () => {
    mockedDiscoverProjects.mockResolvedValue({ indexed: [], discovered: [], query: 'zzz', totalFound: 0 })
    const { result } = renderHook(() => useDiscovery())
    await act(async () => { await result.current.search({ query: 'zzz' }) })
    expect(result.current.result?.totalFound).toBe(0)
    expect(result.current.result?.indexed).toEqual([])
    expect(result.current.result?.discovered).toEqual([])
  })
})
