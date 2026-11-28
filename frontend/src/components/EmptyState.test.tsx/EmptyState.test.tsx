import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { EmptyState } from './EmptyState'

describe('EmptyState', () => {
  it('shows the default message', () => {
    render(<EmptyState />)
    expect(screen.getByText('No projects found.')).toBeTruthy()
  })

  it('shows a custom message', () => {
    render(<EmptyState message="No projects match these filters." />)
    expect(screen.getByText('No projects match these filters.')).toBeTruthy()
  })
})
