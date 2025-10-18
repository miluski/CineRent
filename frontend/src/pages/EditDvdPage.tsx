import { DashboardSidebar } from "@/components/DashboardSidebar";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Spinner } from "@/components/ui/spinner";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import { STATIC_BASE_URL } from "@/config/constants";
import { useUpdateDvd } from "@/hooks/mutations/useUpdateDvd";
import { useGetDvdById } from "@/hooks/queries/useGetDvdById";
import type { DvdDto } from "@/interfaces/responses/DvdDto";
import { zodResolver } from "@hookform/resolvers/zod";
import { Camera, UploadCloud } from "lucide-react";
import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useNavigate, useParams } from "react-router-dom";
import * as z from "zod";
import { DashboardHeader } from "../components/DashboardHeader";
import { useGetAllGenres } from "@/hooks/queries/useGetAllGenres";

const formSchema = z.object({
  title: z
    .string()
    .min(1, "Tytuł jest wymagany.")
    .max(100, "Tytuł nie może być dłuższy niż 100 znaków."),
  directors: z
    .string()
    .min(1, "Reżyser jest wymagany.")
    .max(100, "Pole 'Reżyser' nie może być dłuższe niż 100 znaków."),
  releaseYear: z.coerce
    .number<number>()
    .min(1888, "Rok produkcji musi być po 1888.")
    .max(
      new Date().getFullYear() + 1,
      "Rok produkcji nie może być z przyszłości."
    ),
  description: z
    .string()
    .min(10, "Opis musi mieć co najmniej 10 znaków.")
    .max(1000, "Opis nie może być dłuższy niż 1000 znaków."),
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

export function EditDvdPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const { data: dvd, isLoading: isLoadingDvd } = useGetDvdById(id!);
  const { data: genres, isLoading: isLoadingGenres } = useGetAllGenres();
  const updateDvdMutation = useUpdateDvd();

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      title: "",
      directors: "",
      releaseYear: new Date().getFullYear(),
      description: "",
      rentalPricePerDay: 0,
      genresIdentifiers: [],
      durationMinutes: 0,
      copiesAvailable: 0,
      available: true,
    },
  });

  useEffect(() => {
    if (dvd && genres) {
      const genreIds =
        dvd.genres
          ?.map((gName) => genres.find((g) => g.name === gName)?.id.toString())
          .filter(Boolean) ?? [];

      form.reset({
        title: dvd.title,
        directors: dvd.directors.join(", "),
        releaseYear: dvd.releaseYear,
        description: dvd.description,
        durationMinutes: dvd.durationMinutes,
        rentalPricePerDay: dvd.rentalPricePerDay,
        copiesAvailable: dvd.copiesAvailable,
        available: dvd.available,
        genresIdentifiers: genreIds as string[],
        posterImage: dvd.posterUrl
          ? `${STATIC_BASE_URL}${dvd.posterUrl}`
          : undefined,
      });
    }
  }, [dvd, genres, form]);

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
    const payload: Partial<DvdDto> = {
      ...values,
      directors: values.directors.split(",").map((d) => d.trim()),
      genresIdentifiers: values.genresIdentifiers.map(Number),
    };

    // Don't send posterImage if it's the original URL
    if (payload.posterImage?.startsWith("https://")) {
      delete payload.posterImage;
    }

    updateDvdMutation.mutate(
      { id: id!, dvdData: payload },
      {
        onSuccess: () => {
          navigate("/dashboard");
        },
      }
    );
  }

  if (isLoadingDvd || isLoadingGenres) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Spinner className="size-10 text-primary" />
      </div>
    );
  }

  return (
    <div className="grid min-h-screen w-full md:grid-cols-[220px_1fr] lg:grid-cols-[280px_1fr]">
      <DashboardSidebar />
      <div className="flex flex-col">
        <DashboardHeader />
        <main className="flex flex-1 flex-col gap-4 p-4 lg:gap-6 lg:p-6 animate-in fade-in-0 slide-in-from-bottom-4 duration-500 fill-mode-backwards">
          <div className="flex items-center">
            <h1 className="text-lg font-semibold md:text-2xl">Edytuj DVD</h1>
          </div>
          <Card>
            <CardHeader>
              <CardTitle>Edycja filmu: {dvd?.title}</CardTitle>
              <CardDescription>
                Zaktualizuj poniższe pola, aby zmienić dane filmu.
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
                              className="group flex items-center justify-center w-full h-full min-h-[300px] border-2 border-dashed rounded-lg cursor-pointer hover:bg-muted/50 relative"
                            >
                              <Input
                                id="poster-upload"
                                type="file"
                                className="sr-only"
                                onChange={handlePosterChange}
                                accept="image/png, image/jpeg"
                              />
                              {posterImageValue ? (
                                <>
                                  <img
                                    src={posterImageValue}
                                    alt="Podgląd okładki"
                                    className="object-cover w-full h-full rounded-lg"
                                  />
                                  <div className="absolute inset-0 bg-black/50 flex flex-col items-center justify-center text-white opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                                    <Camera className="w-12 h-12" />
                                    <p className="mt-2 text-sm font-semibold">
                                      Zmień okładkę
                                    </p>
                                  </div>
                                </>
                              ) : (
                                <div className="text-center">
                                  <UploadCloud className="w-12 h-12 mx-auto text-muted-foreground" />
                                  <p className="mt-2 text-sm text-muted-foreground">
                                    Kliknij, aby dodać okładkę
                                  </p>
                                </div>
                              )}
                            </label>
                          </FormControl>
                          <FormMessage />
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
                            <FormMessage className="h-5" />
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

                    <div>
                      <FormLabel>Gatunki</FormLabel>
                      <div className="mt-2 grid grid-cols-2 gap-2 sm:grid-cols-3 md:grid-cols-4">
                        {genres?.map((genre) => (
                          <FormField
                            key={genre.id}
                            control={form.control}
                            name="genresIdentifiers"
                            render={({ field }) => (
                              <FormItem className="flex flex-row items-center space-x-3 space-y-0">
                                <FormControl>
                                  <Checkbox
                                    checked={field.value?.includes(
                                      String(genre.id)
                                    )}
                                    onCheckedChange={(checked) => {
                                      const genreIdStr = String(genre.id);
                                      return checked
                                        ? field.onChange([
                                            ...(field.value ?? []),
                                            genreIdStr,
                                          ])
                                        : field.onChange(
                                            field.value?.filter(
                                              (value) => value !== genreIdStr
                                            )
                                          );
                                    }}
                                  />
                                </FormControl>
                                <FormLabel className="font-normal">
                                  {genre.name}
                                </FormLabel>
                              </FormItem>
                            )}
                          />
                        ))}
                      </div>
                      <FormMessage className="h-5 mt-2" />
                    </div>

                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
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
                        disabled={updateDvdMutation.isPending}
                      >
                        {updateDvdMutation.isPending
                          ? "Zapisywanie..."
                          : "Zapisz zmiany"}
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
