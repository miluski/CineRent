import { format } from 'date-fns';
import { AlertCircle, Calendar, CheckCircle, Clock, Hash, Loader, XCircle } from 'lucide-react';
import { useState } from 'react';

import { DashboardHeader } from '@/components/DashboardHeader';
import { DashboardSidebar } from '@/components/DashboardSidebar';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Spinner } from '@/components/ui/spinner';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import type { ReservationStatus } from '@/enums/ReservationStatus';
import { useCancelReservation } from '@/hooks/mutations/useCancelReservation';
import { useGetUserReservations } from '@/hooks/queries/useGetUserReservations';

const statusConfig = {
  PENDING: {
    label: 'Oczekująca',
    color: 'bg-yellow-500',
    icon: <Clock className="mr-2 size-4" />,
  },
  ACCEPTED: {
    label: 'Zaakceptowana',
    color: 'bg-green-500',
    icon: <CheckCircle className="mr-2 size-4" />,
  },
  REJECTED: {
    label: 'Odrzucona',
    color: 'bg-red-500',
    icon: <XCircle className="mr-2 size-4" />,
  },
  CANCELLED: {
    label: 'Anulowana',
    color: 'bg-gray-500',
    icon: <XCircle className="mr-2 size-4" />,
  },
};

export function UserReservationsPage() {
  const [filter, setFilter] = useState<ReservationStatus | 'ALL'>('ALL');
  const {
    data: reservations,
    isLoading,
    isError,
  } = useGetUserReservations(filter === 'ALL' ? undefined : filter);
  const { mutate: cancelReservation, isPending: isCancelling } = useCancelReservation();

  const handleCancel = (reservationId: number) => {
    cancelReservation(String(reservationId));
  };

  const renderContent = () => {
    if (isLoading) {
      return (
        <div className="flex justify-center items-center h-64">
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
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {reservations.map((reservation) => {
          const statusInfo = statusConfig[reservation.status as keyof typeof statusConfig];
          return (
            <Card
              key={reservation.id}
              className="reservation-card relative flex flex-col transition-all duration-300 hover:shadow-lg cursor-pointer"
            >
              <CardHeader>
                <div className="flex justify-between items-start">
                  <CardTitle className="line-clamp-2">{reservation.dvdTitle}</CardTitle>
                  <Badge className={`${statusInfo.color} text-white`}>
                    {statusInfo.icon}
                    {statusInfo.label}
                  </Badge>
                </div>
                <CardDescription>
                  Złożono: {format(new Date(reservation.createdAt), 'dd.MM.yyyy')}
                </CardDescription>
              </CardHeader>
              <CardContent className="flex-grow space-y-3">
                <div className="flex items-center text-sm">
                  <Calendar className="mr-2 size-4 text-muted-foreground" />
                  <span>
                    {format(new Date(reservation.rentalStart), 'dd.MM.yyyy')} -{' '}
                    {format(new Date(reservation.rentalEnd), 'dd.MM.yyyy')}
                  </span>
                </div>
                <div className="flex items-center text-sm">
                  <Hash className="mr-2 size-4 text-muted-foreground" />
                  <span>Liczba sztuk: {reservation.count}</span>
                </div>
              </CardContent>
              {reservation.status === 'PENDING' && (
                <CardFooter className="reservation-footer absolute bottom-0 left-0 right-0 p-4 bg-gradient-to-t from-background via-background/80 to-transparent opacity-0 transition-opacity duration-300">
                  <Button
                    variant="destructive"
                    className="w-full"
                    onClick={() => handleCancel(reservation.id)}
                    disabled={isCancelling}
                  >
                    {isCancelling ? (
                      <Loader className="mr-2 size-4 animate-spin" />
                    ) : (
                      <AlertCircle className="mr-2 size-4" />
                    )}
                    Anuluj rezerwację
                  </Button>
                </CardFooter>
              )}
            </Card>
          );
        })}
      </div>
    );
  };

  return (
    <div className="grid min-h-screen w-full md:grid-cols-[220px_1fr] lg:grid-cols-[280px_1fr]">
      <DashboardSidebar />
      <div className="flex flex-col">
        <DashboardHeader />
        <main className="flex flex-1 flex-col gap-4 p-4 lg:gap-6 lg:p-6 animate-in fade-in-0 slide-in-from-bottom-4 duration-500">
          <div className="flex items-center justify-between">
            <h1 className="text-lg font-semibold md:text-2xl">Moje rezerwacje</h1>
            <Tabs
              defaultValue="ALL"
              onValueChange={(value) => setFilter(value as ReservationStatus | 'ALL')}
            >
              <TabsList>
                <TabsTrigger value="ALL">Wszystkie</TabsTrigger>
                <TabsTrigger value="PENDING">Oczekujące</TabsTrigger>
                <TabsTrigger value="ACCEPTED">Zaakceptowane</TabsTrigger>
                <TabsTrigger value="REJECTED">Odrzucone</TabsTrigger>
                <TabsTrigger value="CANCELLED">Anulowane</TabsTrigger>
              </TabsList>
            </Tabs>
          </div>
          {renderContent()}
        </main>
      </div>
    </div>
  );
}
