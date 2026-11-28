import { describe, it, expect } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { ProjectCard } from './ProjectCard'
import { CompareProvider } from '../context/CompareContext'
import type { Project } from '../types/project'

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
  topics: ['react', 'typescript', 'open-source'],
  lastUpdated: '2026-11-20',
  category: 'DevTools',
  healthScore: 92,
  healthLabel: 'Healthy',
  openIssues: 7,
}

function renderCard(p: Project = project, badge?: 'indexed' | 'discovered') {
  return render(
    <MemoryRouter>
      <CompareProvider>
        <ProjectCard project={p} badge={badge} />
      </CompareProvider>
    </MemoryRouter>,
  )
}

describe('ProjectCard', () => {
  it('renders the name, description, category, licence, language, topics, stars, forks, health badge and open issues', () => {
    renderCard()
    expect(screen.getByText('OSS Atlas')).toBeTruthy()
    expect(screen.getByText('Discover open source projects.')).toBeTruthy()
    expect(screen.getByText('DevTools')).toBeTruthy()
    expect(screen.getByText('MIT')).toBeTruthy()
    expect(screen.getByText('TypeScript')).toBeTruthy()
    expect(screen.getByText('react')).toBeTruthy()
    expect(screen.getByText('typescript')).toBeTruthy()
    expect(screen.getByText('open-source')).toBeTruthy()
    expect(screen.getByText('1.2k')).toBeTruthy()
    expect(screen.getByText('45')).toBeTruthy()
    expect(screen.getByText('Healthy')).toBeTruthy()
    expect(screen.getByText('7')).toBeTruthy()
  })

  it('links to the project page', () => {
    renderCard()
    const link = screen.getByRole('link', { name: /OSS Atlas/ })
    expect(link.getAttribute('href')).toBe('/projects/oss-atlas')
  })

  it('toggles the compare button when clicked', () => {
    renderCard()
    const addButton = screen.getByRole('button', { name: 'Add OSS Atlas to comparison' })
    fireEvent.click(addButton)
    expect(screen.getByRole('button', { name: 'Remove OSS Atlas from comparison' })).toBeTruthy()
    expect(screen.getByText('✓')).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'Remove OSS Atlas from comparison' }))
    expect(screen.getByRole('button', { name: 'Add OSS Atlas to comparison' })).toBeTruthy()
  })

  it('shows how many extra topics there are when a project has more than four', () => {
    const manyTopics: Project = {
      ...project,
      topics: ['react', 'typescript', 'open-source', 'vite', 'testing', 'ci'],
    }
    renderCard(manyTopics)
    expect(screen.getByText('react')).toBeTruthy()
    expect(screen.getByText('vite')).toBeTruthy()
    expect(screen.getByText('+2')).toBeTruthy()
    expect(screen.queryByText('testing')).toBeNull()
  })

  it('shows the Indexed badge when the badge prop is set', () => {
    renderCard(project, 'indexed')
    expect(screen.getByText('Indexed')).toBeTruthy()
  })

  it('renders an external link for projects whose id is a URL', () => {
    const external: Project = { ...project, id: 'https://github.com/example/repo', name: 'External Repo' }
    renderCard(external)
    const link = screen.getByRole('link', { name: /External Repo/ })
    expect(link.getAttribute('href')).toBe('https://github.com/vishnu-593/oss-atlas')
    expect(link.getAttribute('target')).toBe('_blank')
  })
})
