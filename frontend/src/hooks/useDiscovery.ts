import { useState } from 'react'
import type { DiscoveryRequest, DiscoveryResult } from '../types/project'
import { discoverProjects } from '../services/discoveryService'

export function useDiscovery() {
  const [result, setResult] = useState<DiscoveryResult | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function search(request: DiscoveryRequest) {
    if (!request.query.trim()) return
    setLoading(true)
    setError(null)
    try {
      const data = await discoverProjects(request)
      setResult(data)
    } catch (err) {
      setError(String(err))
    } finally {
      setLoading(false)
    }
  }

  return { result, loading, error, search }
}
