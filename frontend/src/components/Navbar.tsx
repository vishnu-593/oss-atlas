import { Link, NavLink } from 'react-router-dom'
import { useBookmarks } from '../hooks/useBookmarks'
import './Navbar.css'

export function Navbar() {
  const { bookmarks } = useBookmarks()

  return (
    <header className="navbar">
      <div className="navbar-inner">

        <Link to="/" className="navbar-brand">
          <svg className="brand-mark" viewBox="0 0 36 36" fill="none" aria-hidden="true">
            <path d="M18 3L32 10.5V25.5L18 33L4 25.5V10.5L18 3Z" fill="url(#lg)" />
            <circle cx="18" cy="18" r="3" fill="white" />
            <line x1="18" y1="15" x2="18" y2="8"  stroke="white" strokeWidth="1.6" strokeLinecap="round" opacity="0.7"/>
            <line x1="15.4" y1="19.5" x2="9.5" y2="23" stroke="white" strokeWidth="1.6" strokeLinecap="round" opacity="0.7"/>
            <line x1="20.6" y1="19.5" x2="26.5" y2="23" stroke="white" strokeWidth="1.6" strokeLinecap="round" opacity="0.7"/>
            <circle cx="18" cy="7"   r="1.8" fill="white" opacity="0.9"/>
            <circle cx="8.5" cy="24" r="1.8" fill="white" opacity="0.9"/>
            <circle cx="27.5" cy="24" r="1.8" fill="white" opacity="0.9"/>
            <defs>
              <linearGradient id="lg" x1="4" y1="3" x2="32" y2="33" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stopColor="#22c55e"/>
                <stop offset="100%" stopColor="#15803d"/>
              </linearGradient>
            </defs>
          </svg>
          <div className="brand-text">
            <span className="brand-name">OSS<span className="brand-name-accent"> Atlas</span></span>
            <span className="brand-sub">Open Source Discovery</span>
          </div>
        </Link>

        <nav className="navbar-nav" aria-label="Main navigation">
          <NavLink to="/explore" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            Explore
          </NavLink>
          <NavLink to="/discover" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            Discover
          </NavLink>
          <NavLink to="/bookmarks" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            Bookmarks
            {bookmarks.length > 0 && (
              <span className="nav-badge">{bookmarks.length}</span>
            )}
          </NavLink>
        </nav>

      </div>
    </header>
  )
}
