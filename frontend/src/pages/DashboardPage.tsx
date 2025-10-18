import { DashboardHeader } from '@/components/DashboardHeader';
import { DashboardSidebar } from '@/components/DashboardSidebar';
import { Skeleton } from '@/components/ui/skeleton';
import { STATIC_BASE_URL } from '@/config/constants';
import { useAuth } from '@/contexts/AuthContext';
import { useGetDvds } from '@/hooks/queries/useGetDvds';
import { useState } from 'react';
import { Link } from 'react-router-dom';

function DvdCardSkeleton() {
  return <Skeleton className="group relative rounded-lg overflow-hidden shadow-lg aspect-[2/3]" />;
}

export function DashboardPage() {
  const { isAdmin } = useAuth();

  const [selectedGenres, setSelectedGenres] = useState<number[]>([]);
  const [searchPhrase, setSearchPhrase] = useState('');

  const {
    data: dvds,
    isLoading,
    isError,
  } = useGetDvds({
    'genres-ids': selectedGenres.length > 0 ? selectedGenres : undefined,
    'search-phrase': searchPhrase || undefined,
  });

  const handleGenreChange = (genreId: number, checked: boolean) => {
    setSelectedGenres((prev) =>
      checked ? [...prev, genreId] : prev.filter((id) => id !== genreId)
    );
  };

  const handleSearchChange = (phrase: string) => {
    setSearchPhrase(phrase);
  };

  return (
    <div className="grid min-h-screen w-full md:grid-cols-[220px_1fr] lg:grid-cols-[280px_1fr]">
      <DashboardSidebar selectedGenres={selectedGenres} onGenreChange={handleGenreChange} />
      <div className="flex flex-col">
        <DashboardHeader
          searchPhrase={searchPhrase}
          onSearchChange={handleSearchChange}
          selectedGenres={selectedGenres}
          onGenreChange={handleGenreChange}
        />
        <main className="flex flex-1 flex-col gap-4 p-4 lg:gap-6 lg:p-6 animate-in fade-in-0 slide-in-from-bottom-4 duration-500 fill-mode-backwards">
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {isLoading && Array.from({ length: 8 }).map((_, i) => <DvdCardSkeleton key={i} />)}
            {isError && <p>Error fetching data.</p>}
            {dvds?.map((dvd) => (
              <div
                key={dvd.id}
                className="group relative rounded-lg overflow-hidden shadow-lg transition-all duration-300 hover:shadow-2xl hover:scale-[1.02]"
              >
                {/* Poster Image */}
                <div className="aspect-[2/3] bg-gradient-to-br from-gray-800 to-gray-900 relative overflow-hidden">
                  {dvd.posterUrl ? (
                    <img
                      draggable={false}
                      src={`${STATIC_BASE_URL}${dvd.posterUrl}`}
                      alt={dvd.title}
                      className="w-full h-full object-cover select-none"
                    />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-gray-500">
                      <p>No Poster</p>
                    </div>
                  )}

                  {/* Overlay with gradient */}
                  <div className="absolute inset-0 bg-gradient-to-t from-black via-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />

                  {/* Content Overlay */}
                  <div className="absolute inset-0 flex flex-col justify-end p-4 translate-y-4 opacity-0 group-hover:translate-y-0 group-hover:opacity-100 transition-all duration-300">
                    {/* Director/Year Info */}
                    <div className="text-white mb-2">
                      <p className="text-sm font-medium text-gray-300">
                        {dvd.directors?.join(', ') || 'Unknown Director'}
                      </p>
                      <p className="text-sm text-gray-400">{dvd.releaseYear || 'Unknown Year'}</p>
                    </div>

                    {/* Title */}
                    <h3 className="text-white font-bold text-lg mb-1 line-clamp-2">{dvd.title}</h3>

                    {/* Genre */}
                    <p className="text-yellow-400 text-sm font-medium mb-3">
                      {dvd.genres?.join(', ') || 'Genre'}
                    </p>

                    {/* Action Buttons */}
                    <div className="flex flex-col sm:flex-row gap-2 ">
                      <Link
                        to={isAdmin ? `/admin/dvd/edit/${dvd.id}` : `/dvd/${dvd.id}`}
                        className="bg-white text-black px-4 py-2 rounded-md text-sm font-semibold hover:bg-gray-200 transition-colors text-center w-full"
                      >
                        {isAdmin ? 'Edytuj' : 'Sprawdź'}
                      </Link>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </main>
      </div>
    </div>
  );
}
