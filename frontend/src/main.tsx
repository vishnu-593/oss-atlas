import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import { MainLayout } from './layouts/MainLayout'
import { HomePage } from './pages/HomePage'
import { ExplorePage } from './pages/ExplorePage'
import { ProjectDetailsPage } from './pages/ProjectDetailsPage'
import { DiscoveryPage } from './pages/DiscoveryPage'
import './index.css'

const router = createBrowserRouter([
  {
    path: '/',
    element: <MainLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'explore', element: <ExplorePage /> },
      { path: 'projects/:id', element: <ProjectDetailsPage /> },
      { path: 'discover', element: <DiscoveryPage /> },
    ],
  },
])

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
)
