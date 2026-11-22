const CATALOGUE_BASE = import.meta.env.VITE_CATALOGUE_URL ?? 'http://localhost:8081'
const DISCOVERY_BASE = import.meta.env.VITE_DISCOVERY_URL ?? 'http://localhost:8082'
const SOURCE_BASE = import.meta.env.VITE_SOURCE_URL ?? 'http://localhost:8083'

export const API_URLS = { CATALOGUE_BASE, DISCOVERY_BASE, SOURCE_BASE }

export async function fetchJson<T>(url: string): Promise<T> {
  const res = await fetch(url, { signal: AbortSignal.timeout(5000) })
  if (!res.ok) throw new Error(`HTTP ${res.status}: ${url}`)
  return res.json() as Promise<T>
}
