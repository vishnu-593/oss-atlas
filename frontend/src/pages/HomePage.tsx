import { useNavigate, Link } from 'react-router-dom'
import { useState } from 'react'
import './HomePage.css'

const FEATURES = [
  {
    icon: (
      <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
        <rect x="2" y="3" width="16" height="14" rx="2" stroke="currentColor" strokeWidth="1.5"/>
        <path d="M6 7h8M6 10h5M6 13h6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
      </svg>
    ),
    title: 'Structured Catalogue',
    desc: 'Browse a curated catalogue of open-source projects organised by language, licence, and topic.',
  },
  {
    icon: (
      <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
        <circle cx="8.5" cy="8.5" r="5.5" stroke="currentColor" strokeWidth="1.5"/>
        <path d="M13 13l4 4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
      </svg>
    ),
    title: 'Smart Search & Filters',
    desc: 'Filter by keyword, language, licence, and topic. Find the right project in seconds.',
  },
  {
    icon: (
      /* GitHub mark */
      <svg viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
        <path fillRule="evenodd" clipRule="evenodd"
          d="M10 0C4.477 0 0 4.477 0 10c0 4.418 2.865 8.166 6.839 9.489.5.092.682-.217.682-.482
             0-.237-.009-.868-.013-1.703-2.782.604-3.369-1.341-3.369-1.341-.454-1.155-1.11-1.462
             -1.11-1.462-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529
             2.341 1.087 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.11-4.555-4.943
             0-1.091.39-1.984 1.029-2.683-.103-.253-.446-1.27.098-2.647 0 0 .84-.269 2.75 1.025
             A9.578 9.578 0 0110 4.836c.85.004 1.705.115 2.504.337 1.909-1.294 2.747-1.025
             2.747-1.025.546 1.377.203 2.394.1 2.647.64.699 1.028 1.592 1.028 2.683 0 3.842
             -2.339 4.687-4.566 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743
             0 .267.18.578.688.48C17.137 18.163 20 14.418 20 10c0-5.523-4.477-10-10-10z"/>
      </svg>
    ),
    title: 'GitHub Discovery',
    desc: 'Discover repositories beyond the catalogue through GitHub integration, with clear indicators for newly found projects.',
  },
]

function HeroDiagram() {
  return (
    <div className="hero-diagram" aria-hidden="true">
      <div className="diagram-window">
        <div className="window-bar">
          <span className="dot dot-r" />
          <span className="dot dot-y" />
          <span className="dot dot-g" />
        </div>
        <div className="window-body">
          <div className="code-line">
            <span className="cl-kw">const</span>
            <span className="cl-var"> project</span>
            <span className="cl-op"> =</span>
            <span className="cl-fn"> await</span>
            <span className="cl-fn"> catalogue</span>
            <span className="cl-op">.</span>
            <span className="cl-fn">find</span>
            <span>(</span>
            <span className="cl-str">'react'</span>
            <span>)</span>
          </div>
          <div className="code-line mt">
            <span className="cl-kw">return</span>
            <span> {'{'}</span>
          </div>
          <div className="code-line ind">
            <span className="cl-prop">name</span>
            <span>: </span>
            <span className="cl-str">"React"</span>
            <span>,</span>
          </div>
          <div className="code-line ind">
            <span className="cl-prop">stars</span>
            <span>: </span>
            <span className="cl-num">229000</span>
            <span>,</span>
          </div>
          <div className="code-line ind">
            <span className="cl-prop">license</span>
            <span>: </span>
            <span className="cl-str">"MIT"</span>
            <span>,</span>
          </div>
          <div className="code-line ind">
            <span className="cl-prop">language</span>
            <span>: </span>
            <span className="cl-str">"JavaScript"</span>
          </div>
          <div className="code-line">
            <span>{'}'}</span>
          </div>
          <div className="code-line mt cursor-line">
            <span className="cl-comment">// 9 projects indexed</span>
            <span className="cursor" />
          </div>
        </div>
      </div>
      <div className="diagram-pill pill-1">MIT Licensed</div>
      <div className="diagram-pill pill-2">229k ★</div>
      <div className="diagram-pill pill-3">JavaScript</div>
    </div>
  )
}

export function HomePage() {
  const [query, setQuery] = useState('')
  const navigate = useNavigate()

  function handleSearch(e: React.FormEvent) {
    e.preventDefault()
    if (query.trim()) {
      navigate(`/explore?q=${encodeURIComponent(query.trim())}`)
    } else {
      navigate('/explore')
    }
  }

  return (
    <div className="home-page">
      <section className="hero-section">
        <div className="hero-content">
          <div className="hero-badge">Open Source Discovery Platform</div>
          <h1 className="hero-title">
            Find the right<br />
            <span className="accent">open-source tool.</span>
          </h1>
          <p className="hero-sub">
            OSS Atlas indexes and surfaces open-source software so developers can
            discover, compare, and evaluate projects — without the noise.
          </p>
          <form className="hero-search" onSubmit={handleSearch} role="search">
            <svg className="search-icon" viewBox="0 0 16 16" fill="none" aria-hidden="true">
              <circle cx="6.5" cy="6.5" r="4.5" stroke="currentColor" strokeWidth="1.4"/>
              <path d="M10 10l3.5 3.5" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round"/>
            </svg>
            <input
              type="search"
              className="hero-search-input"
              value={query}
              onChange={e => setQuery(e.target.value)}
              placeholder="Search by name, language, topic…"
              aria-label="Search open-source projects"
            />
            <button type="submit" className="hero-search-btn">Search</button>
          </form>
        </div>
        <HeroDiagram />
      </section>

      <section className="features-section">
        <div className="features-inner">
          <div className="features-label">Why OSS Atlas</div>
          <div className="features-grid">
            {FEATURES.map(f => (
              <div key={f.title} className="feature-card">
                <div className="feature-icon">{f.icon}</div>
                <h3>{f.title}</h3>
                <p>{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="cta-section">
        <div className="cta-inner">
          <h2>Start exploring now</h2>
          <p>9 projects indexed. More coming as backend services go live.</p>
          <div className="cta-actions">
            <Link to="/explore" className="btn btn-primary">Browse Catalogue</Link>
            <Link to="/discover" className="btn btn-secondary">Discover Projects</Link>
          </div>
        </div>
      </section>
    </div>
  )
}
