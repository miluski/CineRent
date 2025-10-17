import { useState } from "react";
import { format } from "date-fns";
import { toast } from "sonner";
import { Check, Download, Loader, X } from "lucide-react";

import { DashboardHeader } from "@/components/DashboardHeader";
import { DashboardSidebar } from "@/components/DashboardSidebar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type { ReservationStatus } from "@/enums/ReservationStatus";
import { useAcceptReservation } from "@/hooks/mutations/useAcceptReservation";
import { useDeclineReservation } from "@/hooks/mutations/useDeclineReservation";
import { useGetAllReservations } from "@/hooks/queries/useGetAllReservations";
import { axiosInstance } from "@/interceptor";
import type { TransactionDto } from "@/interfaces/responses/TransactionDto";

const statusConfig: Record<
  ReservationStatus,
  { label: string; className: string }
> = {
  PENDING: {
    label: "Oczekująca",
    className: "bg-yellow-500 hover:bg-yellow-500/80",
  },
  ACCEPTED: {
    label: "Zaakceptowana",
    className: "bg-green-500 hover:bg-green-500/80",
  },
  REJECTED: {
    label: "Odrzucona",
    className: "bg-red-500 hover:bg-red-500/80",
  },
  CANCELLED: {
    label: "Anulowana",
    className: "bg-gray-500 hover:bg-gray-500/80",
  },
};

export const ReservationManagementPage = () => {
  const [filter, setFilter] = useState<ReservationStatus | "ALL">("ALL");
  const {
    data: reservations,
    isLoading,
    isError,
  } = useGetAllReservations(filter === "ALL" ? undefined : filter);

  const { mutate: acceptReservation, isPending: isAccepting } =
    useAcceptReservation();
  const { mutate: declineReservation, isPending: isDeclining } =
    useDeclineReservation();

  const handleDownloadInvoice = async (reservationId: number) => {
    try {
      // First, fetch the user's transactions to find the transaction ID
      const transactionsResponse = await axiosInstance.get<TransactionDto[]>(
        "/transactions"
      );
      const transactions = transactionsResponse.data;

      // Find the transaction associated with this reservation
      const transaction = transactions.find(
        (t) => t.rentalId === reservationId
      );

      if (!transaction) {
        toast.error(
          "Nie znaleziono transakcji dla tej rezerwacji. Wypożyczenie może nie być jeszcze zakończone."
        );
        return;
      }

      // Now fetch the invoice using the transaction ID
      const response = await axiosInstance.post<Blob>(
        `/transactions/bill/${transaction.id}`,
        {
          billType: "INVOICE",
        },
        {
          responseType: "blob",
        }
      );

      const blob = new Blob([response.data], { type: "application/pdf" });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `faktura_${transaction.invoiceId || reservationId}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);

      toast.success("Faktura została pobrana!");
    } catch (error) {
      console.error("Błąd pobierania faktury:", error);
      toast.error(
        "Wystąpił błąd podczas pobierania faktury. Wypożyczenie musi być zakończone, aby wygenerować fakturę."
      );
    }
  };

  const renderContent = () => {
    if (isLoading) {
      return (
        <div className="flex h-64 items-center justify-center">
          <Spinner className="size-10" />
        </div>
      );
    }

    if (isError) {
      return (
        <div className="text-center text-red-500">
          Wystąpił błąd podczas pobierania rezerwacji.
        </div>
      );
    }

    if (!reservations || reservations.length === 0) {
      return (
        <div className="text-center text-muted-foreground">
          Nie znaleziono rezerwacji dla wybranego filtra.
        </div>
      );
    }

    return (
      <div className="rounded-md border overflow-hidden">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="whitespace-nowrap">ID</TableHead>
                <TableHead className="whitespace-nowrap">Tytuł DVD</TableHead>
                <TableHead className="whitespace-nowrap">
                  Data rezerwacji
                </TableHead>
                <TableHead className="whitespace-nowrap">
                  Okres wypożyczenia
                </TableHead>
                <TableHead className="whitespace-nowrap">Status</TableHead>
                <TableHead className="text-right whitespace-nowrap">
                  Akcje
                </TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {reservations.map((reservation) => {
                const statusInfo =
                  statusConfig[reservation.status as ReservationStatus];
                const isActionPending = isAccepting || isDeclining;
                return (
                  <TableRow key={reservation.id}>
                    <TableCell className="font-medium whitespace-nowrap">
                      {reservation.id}
                    </TableCell>
                    <TableCell className="whitespace-nowrap">
                      {reservation.dvdTitle}
                    </TableCell>
                    <TableCell className="whitespace-nowrap">
                      {format(new Date(reservation.createdAt), "dd.MM.yyyy")}
                    </TableCell>
                    <TableCell className="whitespace-nowrap">
                      {format(new Date(reservation.rentalStart), "dd.MM.yyyy")}{" "}
                      - {format(new Date(reservation.rentalEnd), "dd.MM.yyyy")}
                    </TableCell>
                    <TableCell>
                      <Badge className={statusInfo.className}>
                        {statusInfo.label}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      {reservation.status === "PENDING" && (
                        <div className="flex justify-end gap-2 whitespace-nowrap">
                          <Button
                            size="sm"
                            variant="outline"
                            className="border-green-500 text-green-500 hover:bg-green-50 hover:text-green-600"
                            onClick={() =>
                              acceptReservation(String(reservation.id))
                            }
                            disabled={isActionPending}
                          >
                            {isAccepting ? (
                              <Loader className="mr-2 size-4 animate-spin" />
                            ) : (
                              <Check className="mr-2 size-4" />
                            )}
                            Akceptuj
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            className="border-red-500 text-red-500 hover:bg-red-50 hover:text-red-600"
                            onClick={() =>
                              declineReservation(String(reservation.id))
                            }
                            disabled={isActionPending}
                          >
                            {isDeclining ? (
                              <Loader className="mr-2 size-4 animate-spin" />
                            ) : (
                              <X className="mr-2 size-4" />
                            )}
                            Odrzuć
                          </Button>
                        </div>
                      )}
                      {reservation.status === "ACCEPTED" && (
                        <div className="flex justify-end whitespace-nowrap">
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() =>
                              handleDownloadInvoice(reservation.id)
                            }
                            className="border-black text-black hover:bg-pink-50 hover:text-pink-800"
                          >
                            <Download className="mr-2 size-4" />
                            Pobierz fakturę
                          </Button>
                        </div>
                      )}
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </div>
      </div>
    );
  };

  return (
    <div className="grid min-h-screen w-full md:grid-cols-[220px_1fr] lg:grid-cols-[280px_1fr]">
      <DashboardSidebar />
      <div className="flex flex-col overflow-hidden">
        <DashboardHeader />
        <main className="flex flex-1 flex-col gap-4 p-4 lg:gap-6 lg:p-6 animate-in fade-in-0 slide-in-from-bottom-4 duration-500 overflow-auto">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <h1 className="text-lg font-semibold md:text-2xl">
              Zarządzanie rezerwacjami
            </h1>
            <div className="flex items-center gap-2 w-full sm:w-auto">
              <span className="text-sm text-muted-foreground whitespace-nowrap">
                Filtruj:
              </span>
              <Select
                value={filter}
                onValueChange={(value) =>
                  setFilter(value as ReservationStatus | "ALL")
                }
              >
                <SelectTrigger className="w-full sm:w-[180px]">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">Wszystkie</SelectItem>
                  <SelectItem value="PENDING">Oczekujące</SelectItem>
                  <SelectItem value="ACCEPTED">Zaakceptowane</SelectItem>
                  <SelectItem value="REJECTED">Odrzucone</SelectItem>
                  <SelectItem value="CANCELLED">Anulowane</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <div className="overflow-x-auto">{renderContent()}</div>
        </main>
      </div>
    </div>
  );
};
