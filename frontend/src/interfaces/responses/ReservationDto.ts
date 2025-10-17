export interface ReservationDto {
  id: number;
  rentalStart: string;
  rentalEnd: string;
  createdAt: string;
  dvdId: number;
  dvdTitle: string;
  count: number;
  status: string;
}
