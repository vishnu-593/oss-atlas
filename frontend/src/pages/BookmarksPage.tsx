import { useBookmarks } from '../hooks/useBookmarks'
import { ProjectCard } from '../components/ProjectCard'
import { EmptyState } from '../components/EmptyState'
import './BookmarksPage.css'

export function BookmarksPage() {
  const { bookmarks, toggle, isBookmarked } = useBookmarks()

  return (
    <div className="bookmarks-page">
      <div className="bookmarks-header">
        <h1>Bookmarks</h1>
        <p>
          {bookmarks.length === 0
            ? 'No saved projects yet'
            : `${bookmarks.length} saved project${bookmarks.length !== 1 ? 's' : ''}`}
        </p>
      </div>

      {bookmarks.length === 0 ? (
        <EmptyState message="Bookmark any project from Browse Catalogue or Discovery to save it here." />
      ) : (
        <div className="bookmarks-grid">
          {bookmarks.map(p => (
            <ProjectCard
              key={p.id}
              project={p}
              bookmarked={isBookmarked(p.id)}
              onToggleBookmark={toggle}
            />
          ))}
        </div>
      )}
    </div>
  )
}
