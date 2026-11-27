import { useState, useCallback } from 'react'
import type { Project } from '../types/project'

const STORAGE_KEY = 'oss-atlas-bookmarks'

function load(): Project[] {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) ?? '[]') as Project[]
  } catch {
    return []
  }
}

function persist(items: Project[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(items))
}

export function useBookmarks() {
  const [bookmarks, setBookmarks] = useState<Project[]>(load)

  const toggle = useCallback((project: Project) => {
    setBookmarks(prev => {
      const next = prev.some(p => p.id === project.id)
        ? prev.filter(p => p.id !== project.id)
        : [...prev, project]
      persist(next)
      return next
    })
  }, [])

  const isBookmarked = useCallback(
    (id: string) => bookmarks.some(p => p.id === id),
    [bookmarks],
  )

  return { bookmarks, toggle, isBookmarked }
}
