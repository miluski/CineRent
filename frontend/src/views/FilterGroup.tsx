import { Checkbox } from "@/components/ui/checkbox";

const genres = [
  { id: 1, label: "Science-Fiction" },
  { id: 2, label: "Akcja" },
  { id: 3, label: "Dramat" },
  { id: 4, label: "Horror" },
  { id: 5, label: "Thriller" },
];

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
