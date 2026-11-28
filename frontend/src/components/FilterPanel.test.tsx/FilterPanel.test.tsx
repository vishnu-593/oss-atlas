import { useState } from 'react'
import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { FilterPanel } from './FilterPanel'
import type { ProjectFilters } from '../types/project'

describe('FilterPanel', () => {
  it('renders the language, licence and topic controls', () => {
    render(<FilterPanel filters={{}} onChange={() => {}} />)
    expect(screen.getByLabelText('Language')).toBeTruthy()
    expect(screen.getByLabelText('Licence')).toBeTruthy()
    expect(screen.getByLabelText('Topic')).toBeTruthy()
    expect(screen.getByRole('option', { name: 'TypeScript' })).toBeTruthy()
    expect(screen.getByRole('option', { name: 'MIT' })).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Reset filters' })).toBeTruthy()
  })

  it('calls onChange with the selected language', () => {
    const onChange = vi.fn()
    render(<FilterPanel filters={{}} onChange={onChange} />)
    fireEvent.change(screen.getByLabelText('Language'), { target: { value: 'Python' } })
    expect(onChange).toHaveBeenCalledWith({ language: 'Python' })
  })

  it('calls onChange with the selected licence', () => {
    const onChange = vi.fn()
    render(<FilterPanel filters={{}} onChange={onChange} />)
    fireEvent.change(screen.getByLabelText('Licence'), { target: { value: 'MIT' } })
    expect(onChange).toHaveBeenCalledWith({ license: 'MIT' })
  })

  it('typing a topic updates the input and calls onChange with the full value', async () => {
    const onChange = vi.fn()
    function Wrapper() {
      const [filters, setFilters] = useState<Partial<ProjectFilters>>({})
      return (
        <FilterPanel
          filters={filters}
          onChange={(next) => { onChange(next); setFilters(next) }}
        />
      )
    }
    render(<Wrapper />)
    const user = userEvent.setup()
    const topicInput = screen.getByLabelText('Topic') as HTMLInputElement
    await user.type(topicInput, 'database')
    expect(topicInput.value).toBe('database')
    expect(onChange).toHaveBeenLastCalledWith({ topic: 'database' })
  })

  it('resets the filters but keeps the search term', () => {
    const onChange = vi.fn()
    const filters: Partial<ProjectFilters> = { search: 'react', language: 'Python', license: 'MIT', topic: 'database' }
    render(<FilterPanel filters={filters} onChange={onChange} />)
    fireEvent.click(screen.getByRole('button', { name: 'Reset filters' }))
    expect(onChange).toHaveBeenCalledWith({ search: 'react' })
  })
})
