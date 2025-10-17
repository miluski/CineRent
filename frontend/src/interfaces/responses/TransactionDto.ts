export interface TransactionDto {
  id: number;
  invoiceId: string;
  dvdTitle: string;
  rentalPeriodDays: number;
  pricePerDay: number;
  lateFee: number;
  totalAmount: number;
  generatedAt: string;
  pdfUrl: string | null;
  billType: string;
  rentalId: number;
}
