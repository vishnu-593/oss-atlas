import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { ErrorState } from './ErrorState'

describe('ErrorState', () => {
  it('shows the default message', () => {
    render(<ErrorState />)
    expect(screen.getByText('Something went wrong.')).toBeTruthy()
  })

  it('shows a custom message', () => {
    render(<ErrorState message="The catalogue service is down." />)
    expect(screen.getByText('The catalogue service is down.')).toBeTruthy()
  })

  it('shows a retry button and calls onRetry when it is clicked', () => {
    const onRetry = vi.fn()
    render(<ErrorState message="Failed to load." onRetry={onRetry} />)
    const button = screen.getByRole('button', { name: 'Try again' })
    fireEvent.click(button)
    expect(onRetry).toHaveBeenCalledTimes(1)
  })

  it('shows no retry button when onRetry is not provided', () => {
    render(<ErrorState />)
    expect(screen.queryByRole('button', { name: 'Try again' })).toBeNull()
  })
})
