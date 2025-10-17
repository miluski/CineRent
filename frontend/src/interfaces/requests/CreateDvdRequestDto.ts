export interface CreateDvdRequestDto {
  title: string;
  genresIdentifiers: number[];
  releaseYear: number;
  directors: string[];
  description: string;
  durationMinutes: number;
  available: boolean;
  copiesAvailable: number;
  rentalPricePerDay: number;
  posterImage?: string;
}
