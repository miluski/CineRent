import { Link, useLocation } from "react-router-dom";
import { FilterGroup } from "./FilterGroup";
import { WordRotate } from "./ui/word-rotate";

export function DashboardSidebar({
  selectedGenres,
  onGenreChange,
}: Partial<{
  selectedGenres: number[];
  onGenreChange: (genreId: number, checked: boolean) => void;
}>) {
  const pathname = useLocation().pathname;
  const shouldShowFilterGroup = pathname === "/dashboard";

  return (
    <div className="hidden border-r bg-muted/40 md:block">
      <div className="flex h-full max-h-screen flex-col gap-2">
        <div className="flex h-14 items-center border-b px-4 lg:h-[60px] lg:px-6">
          <Link
            to="/dashboard"
            className="flex items-center gap-2 font-semibold w-full"
          >
            <WordRotate
              words={["O.P.A.S.", "One Place, All Studios"]}
              className="text-xl lg:text-xl font-bold text-indigo-500"
            />
          </Link>
        </div>
        <div className="flex-1">
          <nav className="grid items-start px-2 text-sm font-medium lg:px-4">
            {shouldShowFilterGroup && (
              <FilterGroup
                selectedGenres={selectedGenres}
                onGenreChange={onGenreChange}
              />
            )}
          </nav>
        </div>
      </div>
    </div>
  );
}
