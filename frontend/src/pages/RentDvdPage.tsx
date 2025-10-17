import * as React from "react";
import { useParams } from "react-router-dom";
import {
  Bell,
  Calendar,
  CheckCircle,
  Clock,
  Film,
  User,
  Wallet,
  XCircle,
} from "lucide-react";

import { useGetDvdById } from "@/hooks/queries/useGetDvdById";
import { useRentDvd } from "@/hooks/mutations/useRentDvd";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { DashboardSidebar } from "@/components/DashboardSidebar";
import { DashboardHeader } from "@/components/DashboardHeader";
import { toast } from "sonner";
import { DatePicker } from "./date-picker";
import { addDays } from "date-fns";

export function RentDvdPage() {
  const { id } = useParams<{ id: string }>();
  const { data: dvd, isLoading, isError, refetch } = useGetDvdById(id!);
  const rentDvdMutation = useRentDvd(() => refetch());

  const [startDate, setStartDate] = React.useState<Date | undefined>(
    new Date()
  );
  const [endDate, setEndDate] = React.useState<Date | undefined>(
    addDays(new Date(), 7)
  );
  const [count, setCount] = React.useState(1);

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!id || !startDate || !endDate) {
      toast.error("Proszę wybrać poprawny zakres dat.");
      return;
    }
    if (startDate > endDate) {
      toast.error(
        "Data zakończenia nie może być wcześniejsza niż rozpoczęcia."
      );
      return;
    }

    rentDvdMutation.mutate({
      dvdId: Number(id),
      rentalStart: startDate.toISOString(),
      rentalEnd: endDate.toISOString(),
      count,
    });
  };

  const handleNotify = () => {
    toast.info("Zostaniesz powiadomiony o dostępności filmu za pomocą e-mail.");
  };

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Spinner className="size-10 text-primary" />
      </div>
    );
  }

  if (isError || !dvd) {
    return (
      <div className="flex h-screen flex-col items-center justify-center gap-4">
        <h2 className="text-2xl font-semibold">Nie znaleziono filmu</h2>
        <p className="text-muted-foreground">
          Przepraszamy, nie mogliśmy znaleźć filmu o podanym ID.
        </p>
        <Button variant="outline" onClick={() => window.history.back()}>
          Powrót
        </Button>
      </div>
    );
  }

  const isAvailable = dvd.available && dvd.copiesAvailable > 0;

  return (
    <div className="grid min-h-screen w-full md:grid-cols-[220px_1fr] lg:grid-cols-[280px_1fr]">
      <DashboardSidebar />
      <div className="flex flex-col">
        <DashboardHeader />
        <main className="flex flex-1 flex-col gap-4 p-4 lg:gap-6 lg:p-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 lg:gap-12">
            {/* Left Column: Poster */}
            <div className="md:col-span-1">
              <img
                draggable={false}
                src={
                  dvd.posterUrl
                    ? `https://localhost:4443${dvd.posterUrl}`
                    : "https://placehold.co/400x600?text=Brak\\nOkładki"
                }
                alt={`Okładka filmu ${dvd.title}`}
                className="w-full h-auto object-cover rounded-lg shadow-xl"
              />
            </div>

            {/* Right Column: Details */}
            <div className="md:col-span-2 flex flex-col gap-6">
              <div>
                <h1 className="text-4xl font-bold tracking-tight">
                  {dvd.title}
                </h1>
                <div className="mt-2 flex items-center gap-4 text-muted-foreground">
                  <div className="flex items-center gap-2">
                    <User className="size-4" />
                    <span>{dvd.directors.join(", ")}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Calendar className="size-4" />
                    <span>{dvd.releaseYear}</span>
                  </div>
                </div>
              </div>

              <div className="flex flex-wrap gap-2">
                {dvd.genres?.map((genre) => (
                  <Badge key={genre} variant="secondary">
                    {genre}
                  </Badge>
                ))}
              </div>

              <Card>
                <CardHeader>
                  <CardTitle>Opis filmu</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">{dvd.description}</p>
                </CardContent>
              </Card>

              <div className="grid grid-cols-2 gap-4 text-sm">
                <div className="flex items-center gap-3 p-3 bg-muted/50 rounded-lg">
                  <Clock className="size-5 text-primary" />
                  <div>
                    <span className="font-semibold">Czas trwania</span>
                    <p className="text-muted-foreground">
                      {dvd.durationMinutes} min
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-3 p-3 bg-muted/50 rounded-lg">
                  <Wallet className="size-5 text-primary" />
                  <div>
                    <span className="font-semibold">Cena za dzień</span>
                    <p className="text-muted-foreground">
                      {dvd.rentalPricePerDay.toFixed(2)} zł
                    </p>
                  </div>
                </div>
              </div>

              {/* Availability and Actions */}
              <div className="mt-auto pt-6">
                {isAvailable ? (
                  <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                    <div>
                      <Badge className="w-fit self-start bg-green-600 hover:bg-green-700 px-4 py-2 text-sm font-semibold">
                        <CheckCircle className="mr-2 size-5" />
                        <span>Dostępny ({dvd.copiesAvailable} szt.)</span>
                      </Badge>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                      <div>
                        <Label>Data rozpoczęcia</Label>
                        <DatePicker
                          date={startDate}
                          setDate={setStartDate}
                          disabled={(date) => date < new Date()}
                          className="mt-2"
                        />
                      </div>
                      <div>
                        <Label>Data zakończenia</Label>
                        <DatePicker
                          date={endDate}
                          setDate={setEndDate}
                          disabled={(date) =>
                            startDate ? date < startDate : date < new Date()
                          }
                          className="mt-2"
                        />
                      </div>
                      <div>
                        <Label htmlFor="count">Liczba sztuk</Label>
                        <Input
                          id="count"
                          type="number"
                          min="1"
                          max={dvd.copiesAvailable}
                          value={count}
                          onChange={(e) => setCount(Number(e.target.value))}
                          className="mt-2"
                        />
                      </div>
                    </div>

                    <Button
                      size="lg"
                      className="w-full"
                      type="submit"
                      disabled={rentDvdMutation.isPending}
                    >
                      {rentDvdMutation.isPending ? (
                        "Przetwarzanie..."
                      ) : (
                        <>
                          <Film className="mr-2 size-5" />
                          Wypożycz
                        </>
                      )}
                    </Button>
                  </form>
                ) : (
                  <div className="flex flex-col gap-3">
                    <Badge
                      variant="destructive"
                      className="w-fit self-start px-4 py-2 text-sm font-semibold"
                    >
                      <XCircle className="mr-2 size-5" />
                      <span>Niedostępny</span>
                    </Badge>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                      <Button size="lg" disabled className="w-full">
                        Wypożycz
                      </Button>
                      <Button
                        size="lg"
                        variant="outline"
                        className="w-full"
                        onClick={handleNotify}
                      >
                        <Bell className="mr-2 size-5" />
                        Powiadom o dostępności
                      </Button>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
