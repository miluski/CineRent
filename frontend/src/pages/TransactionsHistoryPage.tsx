import { useState } from "react";
import { format } from "date-fns";
import { Loader } from "lucide-react";
import { toast } from "sonner";

import { DashboardHeader } from "@/components/DashboardHeader";
import { DashboardSidebar } from "@/components/DashboardSidebar";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Spinner } from "@/components/ui/spinner";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useGetTransactionsHistory } from "@/hooks/queries/useGetTransactionsHistory";
import { axiosInstance } from "@/interceptor";

export const TransactionsHistoryPage = () => {
  const {
    data: transactions,
    isLoading,
    isError,
  } = useGetTransactionsHistory();

  const [downloadingId, setDownloadingId] = useState<number | null>(null);

  const handleDownloadBill = async (
    transactionId: number,
    billType: string,
    invoiceId: string
  ) => {
    if (!billType) return;
    setDownloadingId(transactionId);
    try {
      const response = await axiosInstance.post<Blob>(
        `/transactions/bill/${transactionId}`,
        { billType },
        { responseType: "blob" }
      );

      const blob = new Blob([response.data], { type: "application/pdf" });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      const fileName =
        billType === "INVOICE"
          ? `faktura_${invoiceId}.pdf`
          : `paragon_${invoiceId}.pdf`;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);

      toast.success("Dokument został pomyślnie pobrany!");
    } catch (error) {
      console.error("Błąd pobierania dokumentu:", error);
      toast.error("Wystąpił błąd podczas pobierania dokumentu.");
    } finally {
      setDownloadingId(null);
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
          Wystąpił błąd podczas pobierania historii transakcji.
        </div>
      );
    }

    if (!transactions || transactions.length === 0) {
      return (
        <div className="text-center text-muted-foreground">
          Nie znaleziono żadnych transakcji.
        </div>
      );
    }

    return (
      <div className="rounded-md border overflow-hidden">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>ID Faktury</TableHead>
                <TableHead>Tytuł DVD</TableHead>
                <TableHead>Data</TableHead>
                <TableHead>Kwota</TableHead>
                <TableHead className="text-right">Akcje</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {transactions.map((transaction) => (
                <TableRow key={transaction.id}>
                  <TableCell className="font-medium">
                    {transaction.invoiceId}
                  </TableCell>
                  <TableCell>{transaction.dvdTitle}</TableCell>
                  <TableCell>
                    {format(new Date(transaction.generatedAt), "dd.MM.yyyy")}
                  </TableCell>
                  <TableCell>{transaction.totalAmount.toFixed(2)} zł</TableCell>
                  <TableCell className="flex justify-end">
                    {transaction.billType && (
                      <Select
                        onValueChange={(value) =>
                          handleDownloadBill(
                            transaction.id,
                            value,
                            transaction.invoiceId
                          )
                        }
                        disabled={downloadingId === transaction.id}
                      >
                        <SelectTrigger className="w-[180px]">
                          {downloadingId === transaction.id ? (
                            <>
                              <Loader className="mr-2 size-4 animate-spin" />
                              Pobieranie...
                            </>
                          ) : (
                            <SelectValue placeholder="Pobierz dokument" />
                          )}
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="INVOICE">
                            Pobierz Fakturę
                          </SelectItem>
                          <SelectItem value="RECEIPT">
                            Pobierz Paragon
                          </SelectItem>
                        </SelectContent>
                      </Select>
                    )}
                  </TableCell>
                </TableRow>
              ))}
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
            Historia transakcji
          </h1>
          {renderContent()}
        </main>
      </div>
    </div>
  );
};
