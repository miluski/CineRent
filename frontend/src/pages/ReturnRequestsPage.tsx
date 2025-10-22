import { format } from 'date-fns';
import { Check, Loader, X } from 'lucide-react';

import { DashboardHeader } from '@/components/DashboardHeader';
import { DashboardSidebar } from '@/components/DashboardSidebar';
import { Button } from '@/components/ui/button';
import { Spinner } from '@/components/ui/spinner';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { useAcceptReturnRequest } from '@/hooks/mutations/useAcceptReturnRequest';
import { useDeclineReturnRequest } from '@/hooks/mutations/useDeclineReturnRequest';
import { useGetAllReturnRequests } from '@/hooks/queries/useGetAllReturnRequests';

export const ReturnRequestsPage = () => {
  const { data: returnRequests, isLoading, isError } = useGetAllReturnRequests();

  const { mutate: acceptReturn, isPending: isAccepting } = useAcceptReturnRequest();
  const { mutate: declineReturn, isPending: isDeclining } = useDeclineReturnRequest();

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
          Wystąpił błąd podczas pobierania próśb o zwrot.
        </div>
      );
    }

    if (!returnRequests || returnRequests.length === 0) {
      return (
        <div className="text-center text-muted-foreground">Brak oczekujących próśb o zwrot.</div>
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
                    ID Wypożyczenia
                  </TableHead>
                  <TableHead className="whitespace-nowrap text-xs sm:text-sm px-2 sm:px-4">
                    Tytuł DVD
                  </TableHead>
                  <TableHead className="whitespace-nowrap text-xs sm:text-sm px-2 sm:px-4">
                    Okres wypożyczenia
                  </TableHead>
                  <TableHead className="text-right whitespace-nowrap text-xs sm:text-sm px-2 sm:px-4">
                    Akcje
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {returnRequests.map((request) => {
                  const isActionPending = isAccepting || isDeclining;
                  return (
                    <TableRow key={request.id}>
                      <TableCell className="font-medium text-xs sm:text-sm px-2 sm:px-4">
                        {request.id}
                      </TableCell>
                      <TableCell className="text-xs sm:text-sm px-2 sm:px-4">
                        {request.dvdTitle}
                      </TableCell>
                      <TableCell className="whitespace-nowrap text-xs sm:text-sm px-2 sm:px-4">
                        {format(new Date(request.rentalStart), 'dd.MM.yyyy')} -{' '}
                        {format(new Date(request.rentalEnd), 'dd.MM.yyyy')}
                      </TableCell>
                      <TableCell className="text-right px-2 sm:px-4">
                        <div className="flex flex-col sm:flex-row justify-end gap-1 sm:gap-2 whitespace-nowrap">
                          <Button
                            size="sm"
                            variant="outline"
                            className="border-green-500 text-green-500 hover:bg-green-50 hover:text-green-600 text-xs px-2 py-1 h-auto"
                            onClick={() => acceptReturn(String(request.id))}
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
                            onClick={() => declineReturn(String(request.id))}
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
      <div className="flex flex-col">
        <DashboardHeader />
        <main className="flex flex-1 flex-col gap-3 sm:gap-4 p-3 sm:p-4 lg:gap-6 lg:p-6 animate-in fade-in-0 slide-in-from-bottom-4 duration-500">
          <h1 className="text-base sm:text-lg font-semibold md:text-2xl">Zarządzanie zwrotami</h1>
          {renderContent()}
        </main>
      </div>
    </div>
  );
};
