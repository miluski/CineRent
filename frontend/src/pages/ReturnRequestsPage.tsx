import { format } from "date-fns";
import { Check, Loader, X } from "lucide-react";

import { DashboardHeader } from "@/components/DashboardHeader";
import { DashboardSidebar } from "@/components/DashboardSidebar";
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
import { useAcceptReturnRequest } from "@/hooks/mutations/useAcceptReturnRequest";
import { useDeclineReturnRequest } from "@/hooks/mutations/useDeclineReturnRequest";
import { useGetAllReturnRequests } from "@/hooks/queries/useGetAllReturnRequests";

export const ReturnRequestsPage = () => {
  const {
    data: returnRequests,
    isLoading,
    isError,
  } = useGetAllReturnRequests();

  const { mutate: acceptReturn, isPending: isAccepting } =
    useAcceptReturnRequest();
  const { mutate: declineReturn, isPending: isDeclining } =
    useDeclineReturnRequest();

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
        <div className="text-center text-muted-foreground">
          Brak oczekujących próśb o zwrot.
        </div>
      );
    }

    return (
      <div className="rounded-md border overflow-hidden">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>ID Wypożyczenia</TableHead>
                <TableHead>Tytuł DVD</TableHead>
                <TableHead>Okres wypożyczenia</TableHead>
                <TableHead className="text-right">Akcje</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {returnRequests.map((request) => {
                const isActionPending = isAccepting || isDeclining;
                return (
                  <TableRow key={request.id}>
                    <TableCell className="font-medium">{request.id}</TableCell>
                    <TableCell>{request.dvdTitle}</TableCell>
                    <TableCell>
                      {format(new Date(request.rentalStart), "dd.MM.yyyy")} -{" "}
                      {format(new Date(request.rentalEnd), "dd.MM.yyyy")}
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button
                          size="sm"
                          variant="outline"
                          className="border-green-500 text-green-500 hover:bg-green-50 hover:text-green-600"
                          onClick={() => acceptReturn(String(request.id))}
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
                          onClick={() => declineReturn(String(request.id))}
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
      <div className="flex flex-col">
        <DashboardHeader />
        <main className="flex flex-1 flex-col gap-4 p-4 lg:gap-6 lg:p-6 animate-in fade-in-0 slide-in-from-bottom-4 duration-500">
          <h1 className="text-lg font-semibold md:text-2xl">
            Zarządzanie zwrotami
          </h1>
          {renderContent()}
        </main>
      </div>
    </div>
  );
};
