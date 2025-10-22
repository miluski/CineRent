import { useState } from 'react';
import { Link } from 'react-router-dom';

import { DashboardHeader } from '@/components/DashboardHeader';
import { DashboardSidebar } from '@/components/DashboardSidebar';
import { Pagination } from '@/components/Pagination';
import { STATIC_BASE_URL } from '@/config/constants';
import { useAuth } from '@/contexts/AuthContext';
import { useGetRecommendations } from '@/hooks/queries/useGetRecommendations';

export const RecommendationsPage = () => {
  const { isAdmin } = useAuth();
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 20;

  const {
    data: recommendationsData,
    isLoading,
    isError,
  } = useGetRecommendations({
    page: currentPage,
    size: pageSize,
  });

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const renderContent = () => {
    if (isLoading) {
      return (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {Array.from({ length: 4 }).map((_, index) => (
            <div key={index} className="relative rounded-lg overflow-hidden shadow-lg">
              <div className="aspect-[2/3] bg-muted animate-pulse" />
            </div>
          ))}
        </div>
      );
    }

    if (isError) {
      return (
        <div className="text-center text-red-500 py-10">
          Wystąpił błąd podczas pobierania rekomendacji.
        </div>
      );
    }

    if (!recommendationsData || recommendationsData.content.length === 0) {
      return (
        <div className="text-center text-muted-foreground py-10">
          Nie znaleziono dla Ciebie żadnych rekomendacji. Obejrzyj więcej filmów, abyśmy mogli
          lepiej poznać Twój gust!
        </div>
      );
    }

    return (
      <>
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {recommendationsData.content.map((dvd) => (
            <div
              key={dvd.id}
              className="dvd-card relative rounded-lg overflow-hidden shadow-lg transition-all duration-300 hover:shadow-2xl hover:scale-[1.02] cursor-pointer"
            >
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
                    <p>Brak plakatu</p>
                  </div>
                )}
                <div className="dvd-overlay absolute inset-0 bg-gradient-to-t from-black via-black/50 to-transparent opacity-0 transition-opacity duration-300" />
                <div className="dvd-details absolute inset-0 flex flex-col justify-end p-4 translate-y-4 opacity-0 transition-all duration-300">
                  <h3 className="text-white font-bold text-lg mb-1 line-clamp-2">{dvd.title}</h3>
                  <p className="text-yellow-400 text-sm font-medium mb-3">
                    {dvd.genres?.join(', ') || 'Brak gatunku'}
                  </p>
                  <Link
                    to={isAdmin ? `/admin/dvd/edit/${dvd.id}` : `/dvd/${dvd.id}`}
                    className="bg-white text-black px-4 py-2 rounded-md text-sm font-semibold hover:bg-gray-200 transition-colors text-center w-full"
                  >
                    {isAdmin ? 'Edytuj' : 'Sprawdź'}
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
        <Pagination
          currentPage={recommendationsData.currentPage}
          totalPages={recommendationsData.totalPages}
          totalElements={recommendationsData.totalElements}
          pageSize={recommendationsData.pageSize}
          hasNext={recommendationsData.hasNext}
          hasPrevious={recommendationsData.hasPrevious}
          onPageChange={handlePageChange}
        />
      </>
    );
  };

  return (
    <div className="grid min-h-screen w-full md:grid-cols-[220px_1fr] lg:grid-cols-[280px_1fr]">
      <DashboardSidebar />
      <div className="flex flex-col">
        <DashboardHeader />
        <main className="flex flex-1 flex-col gap-4 p-4 lg:gap-6 lg:p-6 animate-in fade-in-0 slide-in-from-bottom-4 duration-500">
          <h1 className="text-lg font-semibold md:text-2xl">Rekomendacje</h1>
          {renderContent()}
        </main>
      </div>
    </div>
  );
};
