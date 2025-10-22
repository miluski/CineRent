import { format } from 'date-fns';
import { Check, Loader, X } from 'lucide-react';
import { useState } from 'react';

import { DashboardHeader } from '@/components/DashboardHeader';
import { DashboardSidebar } from '@/components/DashboardSidebar';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Spinner } from '@/components/ui/spinner';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import type { ReservationStatus } from '@/enums/ReservationStatus';
import { useAcceptReservation } from '@/hooks/mutations/useAcceptReservation';
import { useDeclineReservation } from '@/hooks/mutations/useDeclineReservation';
import { useGetAllReservations } from '@/hooks/queries/useGetAllReservations';

const statusConfig: Record<ReservationStatus, { label: string; className: string }> = {
  PENDING: {
    label: 'Oczekująca',
    className: 'bg-yellow-500 hover:bg-yellow-500/80',
  },
  ACCEPTED: {
    label: 'Zaakceptowana',
    className: 'bg-green-500 hover:bg-green-500/80',
  },
  REJECTED: {
    label: 'Odrzucona',
    className: 'bg-red-500 hover:bg-red-500/80',
  },
  CANCELLED: {
    label: 'Anulowana',
    className: 'bg-gray-500 hover:bg-gray-500/80',
  },
};

export const ReservationManagementPage = () => {
  const [filter, setFilter] = useState<ReservationStatus | 'ALL'>('ALL');
  const {
    data: reservations,
    isLoading,
    isError,
  } = useGetAllReservations(filter === 'ALL' ? undefined : filter);

  const { mutate: acceptReservation, isPending: isAccepting } = useAcceptReservation();
  const { mutate: declineReservation, isPending: isDeclining } = useDeclineReservation();

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
        <div className="text-center text-red-500">Wystąpił błąd podczas pobierania rezerwacji.</div>
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
          <div className="inline-block min-w-full align-middle">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="whitespace-nowrap text-xs sm:text-sm px-2 sm:px-4">
                    ID
                  </TableHead>
                  <TableHead className="whitespace-nowrap text-xs sm:text-sm px-2 sm:px-4">
                    Tytuł DVD
                  </TableHead>
                  <TableHead className="whitespace-nowrap text-xs sm:text-sm px-2 sm:px-4">
                    Data rezerwacji
                  </TableHead>
                  <TableHead className="whitespace-nowrap text-xs sm:text-sm px-2 sm:px-4">
                    Okres wypożyczenia
                  </TableHead>
                  <TableHead className="whitespace-nowrap text-xs sm:text-sm px-2 sm:px-4">
                    Status
                  </TableHead>
                  <TableHead className="text-right whitespace-nowrap text-xs sm:text-sm px-2 sm:px-4">
                    Akcje
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {reservations.map((reservation) => {
                  const statusInfo = statusConfig[reservation.status as ReservationStatus];
                  const isActionPending = isAccepting || isDeclining;
                  return (
                    <TableRow key={reservation.id}>
                      <TableCell className="font-medium whitespace-nowrap text-xs sm:text-sm px-2 sm:px-4">
                        {reservation.id}
                      </TableCell>
                      <TableCell className="whitespace-nowrap text-xs sm:text-sm px-2 sm:px-4">
                        {reservation.dvdTitle}
                      </TableCell>
                      <TableCell className="whitespace-nowrap text-xs sm:text-sm px-2 sm:px-4">
                        {format(new Date(reservation.createdAt), 'dd.MM.yyyy')}
                      </TableCell>
                      <TableCell className="whitespace-nowrap text-xs sm:text-sm px-2 sm:px-4">
                        {format(new Date(reservation.rentalStart), 'dd.MM.yyyy')} -{' '}
                        {format(new Date(reservation.rentalEnd), 'dd.MM.yyyy')}
                      </TableCell>
                      <TableCell className="px-2 sm:px-4">
                        <Badge className={`${statusInfo.className} text-[10px] sm:text-xs`}>
                          {statusInfo.label}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-right px-2 sm:px-4">
                        {reservation.status === 'PENDING' && (
                          <div className="flex flex-col sm:flex-row justify-end gap-1 sm:gap-2 whitespace-nowrap">
                            <Button
                              size="sm"
                              variant="outline"
                              className="border-green-500 text-green-500 hover:bg-green-50 hover:text-green-600 text-xs px-2 py-1 h-auto"
                              onClick={() => acceptReservation(String(reservation.id))}
                              disabled={isActionPending}
                            >
                              {isAccepting ? (
                                <Loader className="mr-1 size-3 animate-spin" />
                              ) : (
                                <Check className="mr-1 size-3" />
                              )}
                              <span className="hidden sm:inline">Akceptuj</span>
                              <span className="sm:hidden">✓</span>
                            </Button>
                            <Button
                              size="sm"
                              variant="outline"
                              className="border-red-500 text-red-500 hover:bg-red-50 hover:text-red-600 text-xs px-2 py-1 h-auto"
                              onClick={() => declineReservation(String(reservation.id))}
                              disabled={isActionPending}
                            >
                              {isDeclining ? (
                                <Loader className="mr-1 size-3 animate-spin" />
                              ) : (
                                <X className="mr-1 size-3" />
                              )}
                              <span className="hidden sm:inline">Odrzuć</span>
                              <span className="sm:hidden">✗</span>
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
      </div>
    );
  };

  return (
    <div className="grid min-h-screen w-full md:grid-cols-[220px_1fr] lg:grid-cols-[280px_1fr]">
      <DashboardSidebar />
      <div className="flex flex-col overflow-hidden">
        <DashboardHeader />
        <main className="flex flex-1 flex-col gap-3 sm:gap-4 p-3 sm:p-4 lg:gap-6 lg:p-6 animate-in fade-in-0 slide-in-from-bottom-4 duration-500 overflow-auto">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 sm:gap-4">
            <h1 className="text-base sm:text-lg font-semibold md:text-2xl">
              Zarządzanie rezerwacjami
            </h1>
            <div className="flex items-center gap-2 w-full sm:w-auto">
              <span className="text-xs sm:text-sm text-muted-foreground whitespace-nowrap">
                Filtruj:
              </span>
              <Select
                value={filter}
                onValueChange={(value) => setFilter(value as ReservationStatus | 'ALL')}
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
