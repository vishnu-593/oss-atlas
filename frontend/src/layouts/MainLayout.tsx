import { Outlet } from 'react-router-dom'
import { Navbar } from '../components/Navbar'
import { Footer } from '../components/Footer'
import './MainLayout.css'

export function MainLayout() {
  return (
    <div className="app-shell">
      <Navbar />
      <main className="main-content">
        <Outlet />
      </main>
      <Footer />
    </div>
  )
}
