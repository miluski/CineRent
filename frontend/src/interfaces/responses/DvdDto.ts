export interface DvdDto {
  id?: number;
  title: string;
  genres?: string[];
  genresIdentifiers: number[];
  releaseYear: number;
  directors: string[];
  description: string;
  durationMinutes: number;
  status?: string;
  available: boolean;
  copiesAvailable: number;
  rentalPricePerDay: number;
  posterUrl?: string;
  posterImage?: string;
  addedAt?: string;
  recommendationReason?: string;
  availabilityStatus?: string;
}
