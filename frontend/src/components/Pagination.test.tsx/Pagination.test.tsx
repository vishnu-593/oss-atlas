import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { Pagination } from './Pagination'

describe('Pagination', () => {
  it('shows the current page and the total page count', () => {
    render(<Pagination page={1} totalPages={5} onPageChange={() => {}} />)
    expect(screen.getByText('Page 2 of 5')).toBeTruthy()
  })

  it('renders nothing when there is only one page', () => {
    const { container } = render(<Pagination page={0} totalPages={1} onPageChange={() => {}} />)
    expect(container.firstChild).toBeNull()
  })

  it('disables the previous button on the first page', () => {
    render(<Pagination page={0} totalPages={5} onPageChange={() => {}} />)
    expect((screen.getByRole('button', { name: /Prev/ }) as HTMLButtonElement).disabled).toBe(true)
    expect((screen.getByRole('button', { name: /Next/ }) as HTMLButtonElement).disabled).toBe(false)
  })

  it('disables the next button on the last page', () => {
    render(<Pagination page={4} totalPages={5} onPageChange={() => {}} />)
    expect((screen.getByRole('button', { name: /Next/ }) as HTMLButtonElement).disabled).toBe(true)
    expect((screen.getByRole('button', { name: /Prev/ }) as HTMLButtonElement).disabled).toBe(false)
  })

  it('calls onPageChange with the previous page', () => {
    const onPageChange = vi.fn()
    render(<Pagination page={2} totalPages={5} onPageChange={onPageChange} />)
    fireEvent.click(screen.getByRole('button', { name: /Prev/ }))
    expect(onPageChange).toHaveBeenCalledWith(1)
  })

  it('calls onPageChange with the next page', () => {
    const onPageChange = vi.fn()
    render(<Pagination page={2} totalPages={5} onPageChange={onPageChange} />)
    fireEvent.click(screen.getByRole('button', { name: /Next/ }))
    expect(onPageChange).toHaveBeenCalledWith(3)
  })
})
