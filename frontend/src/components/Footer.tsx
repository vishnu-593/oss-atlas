import { Link } from 'react-router-dom'
import './Footer.css'

export function Footer() {
  return (
    <footer className="footer">
      <div className="footer-inner">
        <div className="footer-brand">
          <svg className="footer-mark" viewBox="0 0 32 32" fill="none" aria-hidden="true">
            <rect width="32" height="32" rx="8" fill="#16a34a"/>
            <circle cx="16" cy="13" r="4" fill="white"/>
            <path d="M16 17c0 0-6 5.5-6 9h12c0-3.5-6-9-6-9z" fill="white" opacity="0.25"/>
            <path d="M16 17c-1.5 2-3.5 4.5-4 6" stroke="white" strokeWidth="1.5" strokeLinecap="round" opacity="0.6"/>
            <path d="M16 17c1.5 2 3.5 4.5 4 6" stroke="white" strokeWidth="1.5" strokeLinecap="round" opacity="0.6"/>
            <circle cx="16" cy="13" r="1.8" fill="#16a34a"/>
          </svg>
          <span className="footer-wordmark">OSS Atlas</span>
        </div>
        <div className="footer-links">
          <Link to="/explore">Explore</Link>
          <Link to="/discover">Discover</Link>
          <span className="footer-sep">·</span>
          <span className="footer-copy">Open Source Discovery Platform</span>
        </div>
      </div>
    </footer>
  )
}
