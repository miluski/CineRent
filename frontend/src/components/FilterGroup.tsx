import { Checkbox } from '@/components/ui/checkbox';
import { useGetAllGenres } from '@/hooks/queries/useGetAllGenres';

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
  const { data: genres } = useGetAllGenres();

  return (
    <>
      <h3 className="my-2 px-2 text-lg font-semibold tracking-tight">Filtry</h3>
      {genres?.map((genre) => (
        <div
          key={genre.id}
          className="flex items-center space-x-2 p-2 cursor-pointer hover:bg-muted/50 rounded-md transition-colors"
        >
          <Checkbox
            id={`${isMobile ? 'mobile-' : ''}${genre.id}`}
            checked={selectedGenres.includes(genre.id)}
            onCheckedChange={(checked) => onGenreChange?.(genre.id, !!checked)}
          />
          <label
            htmlFor={`${isMobile ? 'mobile-' : ''}${genre.id}`}
            className={`text-sm font-medium leading-none cursor-pointer ${
              !isMobile && 'peer-disabled:cursor-not-allowed peer-disabled:opacity-70'
            }`}
          >
            {genre.name}
          </label>
        </div>
      ))}
    </>
  );
}
