import { format } from 'date-fns';
import { Calendar, CheckCircle, Clock, Hash, Loader, RotateCcw } from 'lucide-react';
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
import { useReturnRequest } from '@/hooks/mutations/useReturnRequest';
import { useGetRentals } from '@/hooks/queries/useGetRentals';

type FilterType = 'CURRENT' | 'HISTORICAL';

const statusConfig = {
  ACTIVE: {
    label: 'Aktywne',
    color: 'bg-green-500',
    icon: <CheckCircle className="mr-2 size-4" />,
  },
  RETURN_REQUESTED: {
    label: 'Oczekuje na zwrot',
    color: 'bg-yellow-500',
    icon: <Clock className="mr-2 size-4" />,
  },
};

export const RentalsPage = () => {
  const [filter, setFilter] = useState<FilterType>('CURRENT');
  const {
    data: rentals,
    isLoading,
    isError,
  } = useGetRentals(filter === 'HISTORICAL' ? 'HISTORICAL' : undefined);
  const { mutate: returnRequest, isPending: isReturning } = useReturnRequest();

  const handleReturnRequest = (rentalId: number) => {
    returnRequest(String(rentalId));
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
        <div className="text-center text-red-500">Wystąpił błąd podczas pobierania wypożyczeń.</div>
      );
    }

    if (!rentals || rentals.length === 0) {
      return (
        <div className="text-center text-muted-foreground">
          Nie znaleziono wypożyczeń dla wybranego filtra.
        </div>
      );
    }

    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {rentals.map((rental) => {
          const statusInfo = statusConfig[rental.status as keyof typeof statusConfig];
          return (
            <Card
              key={rental.id}
              className="rental-card relative flex flex-col transition-all duration-300 hover:shadow-lg cursor-pointer"
            >
              <CardHeader>
                <div className="flex justify-between items-start">
                  <CardTitle className="line-clamp-2">{rental.dvdTitle}</CardTitle>
                  {statusInfo && (
                    <Badge className={`${statusInfo.color} text-white`}>
                      {statusInfo.icon}
                      {statusInfo.label}
                    </Badge>
                  )}
                </div>
                <CardDescription>
                  Wypożyczono: {format(new Date(rental.createdAt), 'dd.MM.yyyy')}
                </CardDescription>
              </CardHeader>
              <CardContent className="flex-grow space-y-3">
                <div className="flex items-center text-sm">
                  <Calendar className="mr-2 size-4 text-muted-foreground" />
                  <span>
                    {format(new Date(rental.rentalStart), 'dd.MM.yyyy')} -{' '}
                    {format(new Date(rental.rentalEnd), 'dd.MM.yyyy')}
                  </span>
                </div>
                <div className="flex items-center text-sm">
                  <Hash className="mr-2 size-4 text-muted-foreground" />
                  <span>Liczba sztuk: {rental.count}</span>
                </div>
              </CardContent>
              {rental.status === 'ACTIVE' && (
                <CardFooter className="rental-footer absolute bottom-0 left-0 right-0 p-4 bg-gradient-to-t from-background via-background/80 to-transparent opacity-0 transition-opacity duration-300">
                  <Button
                    variant="outline"
                    className="w-full border-primary text-primary hover:bg-secondary hover:text-primary"
                    onClick={() => handleReturnRequest(rental.id)}
                    disabled={isReturning}
                  >
                    {isReturning ? (
                      <Loader className="mr-2 size-4 animate-spin" />
                    ) : (
                      <RotateCcw className="mr-2 size-4" />
                    )}
                    Złóż prośbę o zwrot
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
            <h1 className="text-lg font-semibold md:text-2xl">Moje wypożyczenia</h1>
            <Tabs defaultValue="CURRENT" onValueChange={(value) => setFilter(value as FilterType)}>
              <TabsList>
                <TabsTrigger value="CURRENT">Aktywne</TabsTrigger>
                <TabsTrigger value="HISTORICAL">Historia</TabsTrigger>
              </TabsList>
            </Tabs>
          </div>
          {renderContent()}
        </main>
      </div>
    </div>
  );
};
