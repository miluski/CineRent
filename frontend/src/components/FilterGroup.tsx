import { Checkbox } from "@/components/ui/checkbox";
import { genres } from "@/utils/genres";

interface FilterGroupProps {
  isMobile?: boolean;
}

export function FilterGroup({ isMobile = false }: FilterGroupProps) {
  return (
    <>
      <h3 className="my-2 px-2 text-lg font-semibold tracking-tight">Filtry</h3>
      {genres.map((genre) => (
        <div key={genre.id} className="flex items-center space-x-2 p-2">
          <Checkbox id={`${isMobile ? "mobile-" : ""}${genre.id}`} />
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
