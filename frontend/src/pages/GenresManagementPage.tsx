import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Loader, PlusCircle, Trash2 } from "lucide-react";

import { DashboardHeader } from "@/components/DashboardHeader";
import { DashboardSidebar } from "@/components/DashboardSidebar";
import { useGetAllGenres } from "@/hooks/queries/useGetAllGenres";
import { useCreateGenre } from "@/hooks/mutations/useCreateGenre";
import { useDeleteGenre } from "@/hooks/mutations/useDeleteGenre";
import { Spinner } from "@/components/ui/spinner";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import type { GenreDto } from "@/interfaces/responses/GenreDto";

const formSchema = z.object({
  name: z
    .string()
    .min(5, "Nazwa musi mieć co najmniej 5 znaków.")
    .max(75, "Nazwa może mieć maksymalnie 75 znaków."),
});

export const GenresManagementPage = () => {
  const { data: genres, isLoading, isError } = useGetAllGenres();
  const { mutate: createGenre, isPending: isCreating } = useCreateGenre();
  const { mutate: deleteGenre, isPending: isDeleting } = useDeleteGenre();

  const [deletingGenreId, setDeletingGenreId] = useState<number | null>(null);

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      name: "",
    },
  });

  const onSubmit = (values: z.infer<typeof formSchema>) => {
    createGenre(values, {
      onSuccess: () => {
        form.reset();
      },
    });
  };

  const handleDelete = (genreId: number) => {
    deleteGenre(String(genreId));
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
        <div className="text-center text-red-500">
          Wystąpił błąd podczas pobierania gatunków.
        </div>
      );
    }

    return (
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-[100px]">ID</TableHead>
            <TableHead>Nazwa</TableHead>
            <TableHead className="text-right w-[100px]">Akcje</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {genres?.map((genre: GenreDto) => (
            <TableRow key={genre.id}>
              <TableCell className="font-medium">{genre.id}</TableCell>
              <TableCell>{genre.name}</TableCell>
              <TableCell className="text-right">
                <AlertDialog>
                  <AlertDialogTrigger asChild>
                    <Button
                      variant="destructive"
                      size="icon"
                      onClick={() => setDeletingGenreId(genre.id)}
                      disabled={isDeleting && deletingGenreId === genre.id}
                    >
                      {isDeleting && deletingGenreId === genre.id ? (
                        <Loader className="size-4 animate-spin" />
                      ) : (
                        <Trash2 className="size-4" />
                      )}
                    </Button>
                  </AlertDialogTrigger>
                  <AlertDialogContent>
                    <AlertDialogHeader>
                      <AlertDialogTitle>
                        Czy na pewno chcesz usunąć ten gatunek?
                      </AlertDialogTitle>
                      <AlertDialogDescription>
                        Tej operacji nie można cofnąć. Spowoduje to trwałe
                        usunięcie gatunku z bazy danych.
                      </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                      <AlertDialogCancel>Anuluj</AlertDialogCancel>
                      <AlertDialogAction onClick={() => handleDelete(genre.id)}>
                        Usuń
                      </AlertDialogAction>
                    </AlertDialogFooter>
                  </AlertDialogContent>
                </AlertDialog>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    );
  };

  return (
    <div className="grid min-h-screen w-full md:grid-cols-[220px_1fr] lg:grid-cols-[280px_1fr]">
      <DashboardSidebar />
      <div className="flex flex-col">
        <DashboardHeader />
        <main className="flex flex-1 flex-col gap-4 p-4 lg:gap-6 lg:p-6 animate-in fade-in-0 slide-in-from-bottom-4 duration-500">
          <h1 className="text-lg font-semibold md:text-2xl">
            Zarządzanie gatunkami
          </h1>
          <div className="border rounded-lg p-4">
            <h2 className="text-md font-semibold mb-4">Dodaj nowy gatunek</h2>
            <Form {...form}>
              <form
                onSubmit={form.handleSubmit(onSubmit)}
                className="flex items-start gap-4"
              >
                <FormField
                  control={form.control}
                  name="name"
                  render={({ field }) => (
                    <FormItem className="flex-grow">
                      <FormLabel>Nazwa gatunku</FormLabel>
                      <FormControl>
                        <Input
                          placeholder="np. Komedia romantyczna"
                          {...field}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <Button type="submit" disabled={isCreating} className="mt-8">
                  {isCreating ? (
                    <Loader className="mr-2 size-4 animate-spin" />
                  ) : (
                    <PlusCircle className="mr-2 size-4" />
                  )}
                  Dodaj
                </Button>
              </form>
            </Form>
          </div>
          <div className="border rounded-lg">{renderContent()}</div>
        </main>
      </div>
    </div>
  );
};
