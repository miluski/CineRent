import { Checkbox } from "@/components/ui/checkbox";
import { genres } from "@/utils/genres";

interface FilterGroupProps {
  selectedGenres?: number[];
  onGenreChange?: (genreId: number, checked: boolean) => void;
  isMobile?: boolean;
}

export function FilterGroup({
  selectedGenres = [],
  onGenreChange,
  isMobile = false,
}: FilterGroupProps) {
  return (
    <>
      <h3 className="my-2 px-2 text-lg font-semibold tracking-tight">Filtry</h3>
      {genres.map((genre) => (
        <div key={genre.id} className="flex items-center space-x-2 p-2">
          <Checkbox
            id={`${isMobile ? "mobile-" : ""}${genre.id}`}
            checked={selectedGenres.includes(genre.id)}
            onCheckedChange={(checked) => onGenreChange?.(genre.id, !!checked)}
          />
          <label
            htmlFor={`${isMobile ? "mobile-" : ""}${genre.id}`}
            className={`text-sm font-medium leading-none ${
              !isMobile &&
              "peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
            }`}
          >
            {genre.label}
          </label>
        </div>
      ))}
    </>
  );
}
