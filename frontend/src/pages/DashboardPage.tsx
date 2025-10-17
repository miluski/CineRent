import { DashboardHeader } from "@/components/DashboardHeader";
import { DashboardSidebar } from "@/components/DashboardSidebar";
import { useAuth } from "@/contexts/AuthContext";
import { useGetDvds } from "@/hooks/queries/useGetDvds";
import { Link } from "react-router-dom";

export function DashboardPage() {
  const { isAdmin } = useAuth();
  const { data: dvds, isLoading, isError } = useGetDvds();

  return (
    <div className="grid min-h-screen w-full md:grid-cols-[220px_1fr] lg:grid-cols-[280px_1fr]">
      <DashboardSidebar />
      <div className="flex flex-col">
        <DashboardHeader />
        <main className="flex flex-1 flex-col gap-4 p-4 lg:gap-6 lg:p-6 animate-in fade-in-0 slide-in-from-bottom-4 duration-500 fill-mode-backwards">
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {isLoading &&
              Array.from({ length: 8 }).map((_, index) => (
                <div
                  key={index}
                  className="relative rounded-lg overflow-hidden shadow-lg"
                >
                  <div className="aspect-[2/3] bg-muted animate-pulse" />
                </div>
              ))}
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
                      src={`https://localhost:4443${dvd.posterUrl}`}
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
                        {dvd.directors?.join(", ") || "Unknown Director"}
                      </p>
                      <p className="text-sm text-gray-400">
                        {dvd.releaseYear || "Unknown Year"}
                      </p>
                    </div>

                    {/* Title */}
                    <h3 className="text-white font-bold text-lg mb-1 line-clamp-2">
                      {dvd.title}
                    </h3>

                    {/* Genre */}
                    <p className="text-yellow-400 text-sm font-medium mb-3">
                      {dvd.genres?.join(", ") || "Genre"}
                    </p>

                    {/* Action Buttons */}
                    <div className="flex flex-col sm:flex-row gap-2 ">
                      <Link
                        to={
                          isAdmin
                            ? `/admin/dvd/edit/${dvd.id}`
                            : `/dvd/${dvd.id}`
                        }
                        className="bg-white text-black px-4 py-2 rounded-md text-sm font-semibold hover:bg-gray-200 transition-colors text-center w-full"
                      >
                        {isAdmin ? "Edytuj" : "Sprawdź"}
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
