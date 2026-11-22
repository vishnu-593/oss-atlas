import { useState, useEffect, useCallback } from 'react'
import type { Project, ProjectFilters, PagedResponse } from '../types/project'
import { getProjects } from '../services/catalogueService'

interface UseProjectsState {
  data: PagedResponse<Project> | null
  loading: boolean
  error: string | null
}

export function useProjects(filters: Partial<ProjectFilters>, page: number) {
  const [state, setState] = useState<UseProjectsState>({ data: null, loading: true, error: null })

  const load = useCallback(async () => {
    setState(s => ({ ...s, loading: true, error: null }))
    try {
      const data = await getProjects(filters, page)
      setState({ data, loading: false, error: null })
    } catch (err) {
      setState({ data: null, loading: false, error: String(err) })
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [JSON.stringify(filters), page])

  useEffect(() => { void load() }, [load])

  return { ...state, reload: load }
}
