import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import * as z from "zod";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { DashboardSidebar } from "@/components/DashboardSidebar";
import { useNavigate } from "react-router-dom";
import { UploadCloud } from "lucide-react";
import { useCreateDvd } from "@/hooks/mutations/useCreateDvd";
import { useGetAllGenres } from "@/hooks/queries/useGetAllGenres";
import { DashboardHeader } from "../components/DashboardHeader";
import { Switch } from "@/components/ui/switch";

const formSchema = z.object({
  title: z
    .string()
    .min(5, "Tytuł musi mieć co najmniej 5 znaków.")
    .max(75, "Tytuł nie może być dłuższy niż 75 znaków."),
  directors: z
    .string()
    .min(1, "Reżyser jest wymagany.")
    .max(100, "Pole 'Reżyser' nie może być dłuższe niż 100 znaków.")
    .refine(
      (value) =>
        value.split(",").every((director) => director.trim().length >= 10),
      {
        message:
          "Każde imię i nazwisko reżysera musi mieć co najmniej 10 znaków.",
      }
    ),
  releaseYear: z.coerce
    .number<number>()
    .min(1888, "Rok produkcji musi być po 1888.")
    .max(
      new Date().getFullYear() + 1,
      "Rok produkcji nie może być z przyszłości."
    ),
  description: z
    .string()
    .min(25, "Opis musi mieć co najmniej 25 znaków.")
    .max(500, "Opis nie może być dłuższy niż 500 znaków."),
  durationMinutes: z.coerce
    .number<number>()
    .min(1, "Czas trwania musi być większy od 0."),
  rentalPricePerDay: z.coerce
    .number<number>()
    .min(0, "Cena nie może być ujemna.")
    .positive("Cena musi być większa od zera."),
  copiesAvailable: z.coerce
    .number<number>()
    .min(0, "Liczba kopii nie może być ujemna."),
  available: z.boolean(), 
  genresIdentifiers: z
    .array(z.string())
    .nonempty("Musisz wybrać co najmniej jeden gatunek."),
  posterImage: z.string().optional(),
});

export function AddDvdPage() {
  const navigate = useNavigate();
  const { data: genres, isLoading: isLoadingGenres } = useGetAllGenres();
  const createDvdMutation = useCreateDvd();

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      title: "",
      directors: "",
      releaseYear: new Date().getFullYear(),
      description: "",
      rentalPricePerDay: 10,
      genresIdentifiers: [],
      durationMinutes: 120,
      copiesAvailable: 1,
      available: true,
    },
  });

  const handlePosterChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        form.setValue("posterImage", reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const posterImageValue = form.watch("posterImage");

  function onSubmit(values: z.infer<typeof formSchema>) {
    const payload = {
      ...values,
      directors: values.directors.split(",").map((d) => d.trim()),
      genresIdentifiers: values.genresIdentifiers.map(Number),
    };
    createDvdMutation.mutate(payload, {
      onSuccess: () => {
        form.reset();
      },
    });
  }

  return (
    <div className="grid min-h-screen w-full md:grid-cols-[220px_1fr] lg:grid-cols-[280px_1fr]">
      <DashboardSidebar />
      <div className="flex flex-col">
        <DashboardHeader />
        <main className="flex flex-1 flex-col gap-4 p-4 lg:gap-6 lg:p-6">
          <div className="flex items-center animate-in fade-in-0 slide-in-from-bottom-4 duration-700 fill-mode-backwards">
            <h1 className="text-lg font-semibold md:text-2xl">
              Dodaj nowe DVD
            </h1>
          </div>
          <Card
            className={
              "animate-in fade-in-0 slide-in-from-bottom-4 duration-700 delay-100 fill-mode-backwards"
            }
          >
            <CardHeader>
              <CardTitle>Nowy film w katalogu</CardTitle>
              <CardDescription>
                Wypełnij poniższe pola, aby dodać nowy film do bazy danych.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Form {...form}>
                <form
                  onSubmit={form.handleSubmit(onSubmit)}
                  className="grid grid-cols-1 gap-8 md:grid-cols-3"
                >
                  <div className="md:col-span-1">
                    <FormField
                      control={form.control}
                      name="posterImage"
                      render={() => (
                        <FormItem>
                          <FormLabel>Okładka filmu</FormLabel>
                          <FormControl>
                            <label
                              htmlFor="poster-upload"
                              className="flex items-center justify-center w-full h-full min-h-[300px] border-2 border-dashed rounded-lg cursor-pointer hover:bg-muted/50 relative"
                            >
                              <Input
                                id="poster-upload"
                                type="file"
                                className="sr-only"
                                onChange={handlePosterChange}
                                accept="image/png, image/jpeg"
                              />
                              {posterImageValue ? (
                                <img
                                  src={posterImageValue}
                                  alt="Podgląd okładki"
                                  className="object-cover w-full h-full rounded-lg"
                                />
                              ) : (
                                <div className="text-center">
                                  <UploadCloud className="w-12 h-12 mx-auto text-muted-foreground" />
                                  <p className="mt-2 text-sm text-muted-foreground">
                                    Kliknij, aby dodać okładkę
                                  </p>
                                  <p className="text-xs text-muted-foreground">
                                    PNG, JPG (MAX. 2MB)
                                  </p>
                                </div>
                              )}
                            </label>
                          </FormControl>
                          <FormMessage className="h-5" />
                        </FormItem>
                      )}
                    />
                  </div>
                  <div className="space-y-4 md:col-span-2">
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                      <FormField
                        control={form.control}
                        name="title"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Tytuł</FormLabel>
                            <FormControl>
                              <Input placeholder="Wpisz tytuł" {...field} />
                            </FormControl>
                            <FormMessage className="h-8" />
                          </FormItem>
                        )}
                      />
                      <FormField
                        control={form.control}
                        name="directors"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Reżyser</FormLabel>
                            <FormControl>
                              <Input
                                placeholder="Wpisz reżysera (oddziel przecinkiem)"
                                {...field}
                              />
                            </FormControl>
                            <FormMessage className="h-5" />
                          </FormItem>
                        )}
                      />
                    </div>

                    <FormField
                      control={form.control}
                      name="description"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Opis</FormLabel>
                          <FormControl>
                            <Textarea
                              placeholder="Wpisz opis filmu"
                              className="resize-none"
                              rows={5}
                              {...field}
                            />
                          </FormControl>
                          <FormMessage className="h-5" />
                        </FormItem>
                      )}
                    />

                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                      <FormField
                        control={form.control}
                        name="genresIdentifiers"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Gatunek</FormLabel>
                            <Select
                              onValueChange={(value) => field.onChange([value])}
                              disabled={isLoadingGenres}
                            >
                              <FormControl>
                                <SelectTrigger>
                                  <SelectValue placeholder="Wybierz gatunek" />
                                </SelectTrigger>
                              </FormControl>
                              <SelectContent>
                                {genres?.map((genre) => (
                                  <SelectItem
                                    key={genre.id}
                                    value={String(genre.id)}
                                  >
                                    {genre.name}
                                  </SelectItem>
                                ))}
                              </SelectContent>
                            </Select>
                            <FormMessage className="h-8" />
                          </FormItem>
                        )}
                      />
                      <FormField
                        control={form.control}
                        name="releaseYear"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Rok produkcji</FormLabel>
                            <FormControl>
                              <Input type="number" {...field} />
                            </FormControl>
                            <FormMessage className="h-5" />
                          </FormItem>
                        )}
                      />
                      <FormField
                        control={form.control}
                        name="durationMinutes"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Czas trwania (min)</FormLabel>
                            <FormControl>
                              <Input type="number" {...field} />
                            </FormControl>
                            <FormMessage className="h-5" />
                          </FormItem>
                        )}
                      />
                    </div>
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                      <FormField
                        control={form.control}
                        name="rentalPricePerDay"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Cena / dzień (PLN)</FormLabel>
                            <FormControl>
                              <Input type="number" step="0.01" {...field} />
                            </FormControl>
                            <FormMessage className="h-5" />
                          </FormItem>
                        )}
                      />
                      <FormField
                        control={form.control}
                        name="copiesAvailable"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Liczba kopii</FormLabel>
                            <FormControl>
                              <Input type="number" {...field} />
                            </FormControl>
                            <FormMessage className="h-5" />
                          </FormItem>
                        )}
                      />
                      <FormField
                        control={form.control}
                        name="available"
                        render={({ field }) => (
                          <FormItem className="flex flex-col pt-2">
                            <FormLabel className="mb-2">Dostępność</FormLabel>
                            <FormControl>
                              <Switch
                                checked={field.value}
                                onCheckedChange={field.onChange}
                              />
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                    </div>
                    <div className="flex justify-end gap-2 pt-4">
                      <Button
                        type="button"
                        variant="outline"
                        onClick={() => navigate(-1)}
                      >
                        Anuluj
                      </Button>
                      <Button
                        type="submit"
                        disabled={createDvdMutation.isPending}
                      >
                        {createDvdMutation.isPending
                          ? "Dodawanie..."
                          : "Dodaj nowy film"}
                      </Button>
                    </div>
                  </div>
                </form>
              </Form>
            </CardContent>
          </Card>
        </main>
      </div>
    </div>
  );
}
