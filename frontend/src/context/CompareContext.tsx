import { createContext, useContext, useState } from 'react'
import type { Project } from '../types/project'

const MAX_COMPARE = 3

interface CompareCtx {
  selected: Project[]
  toggle: (p: Project) => void
  remove: (id: string) => void
  clear: () => void
  isSelected: (id: string) => boolean
  atMax: boolean
}

const CompareContext = createContext<CompareCtx | null>(null)

export function CompareProvider({ children }: { children: React.ReactNode }) {
  const [selected, setSelected] = useState<Project[]>([])

  function toggle(p: Project) {
    setSelected(prev => {
      const idx = prev.findIndex(x => x.id === p.id)
      if (idx >= 0) return prev.filter(x => x.id !== p.id)
      if (prev.length >= MAX_COMPARE) return prev
      return [...prev, p]
    })
  }

  function remove(id: string) {
    setSelected(prev => prev.filter(x => x.id !== id))
  }

  function clear() {
    setSelected([])
  }

  function isSelected(id: string) {
    return selected.some(x => x.id === id)
  }

  return (
    <CompareContext.Provider value={{ selected, toggle, remove, clear, isSelected, atMax: selected.length >= MAX_COMPARE }}>
      {children}
    </CompareContext.Provider>
  )
}

export function useCompare() {
  const ctx = useContext(CompareContext)
  if (!ctx) throw new Error('useCompare must be used within CompareProvider')
  return ctx
}
