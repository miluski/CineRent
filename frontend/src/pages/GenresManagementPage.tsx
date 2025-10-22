import { zodResolver } from '@hookform/resolvers/zod';
import { Loader, PlusCircle, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';

import { DashboardHeader } from '@/components/DashboardHeader';
import { DashboardSidebar } from '@/components/DashboardSidebar';
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
} from '@/components/ui/alert-dialog';
import { Button } from '@/components/ui/button';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Spinner } from '@/components/ui/spinner';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { useCreateGenre } from '@/hooks/mutations/useCreateGenre';
import { useDeleteGenre } from '@/hooks/mutations/useDeleteGenre';
import { useGetAllGenres } from '@/hooks/queries/useGetAllGenres';
import type { GenreDto } from '@/interfaces/responses/GenreDto';

const formSchema = z.object({
  name: z
    .string()
    .min(5, 'Nazwa musi mieć co najmniej 5 znaków.')
    .max(75, 'Nazwa może mieć maksymalnie 75 znaków.'),
});

export const GenresManagementPage = () => {
  const { data: genres, isLoading, isError } = useGetAllGenres();
  const { mutate: createGenre, isPending: isCreating } = useCreateGenre();
  const { mutate: deleteGenre, isPending: isDeleting } = useDeleteGenre();

  const [deletingGenreId, setDeletingGenreId] = useState<number | null>(null);

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      name: '',
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
        <div className="text-center text-red-500">Wystąpił błąd podczas pobierania gatunków.</div>
      );
    }

    return (
      <div className="rounded-md border overflow-hidden">
        <div className="overflow-x-auto">
          <div className="inline-block min-w-full align-middle">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-[60px] sm:w-[100px] text-xs sm:text-sm px-2 sm:px-4">
                    ID
                  </TableHead>
                  <TableHead className="text-xs sm:text-sm px-2 sm:px-4">Nazwa</TableHead>
                  <TableHead className="text-right w-[80px] sm:w-[100px] text-xs sm:text-sm px-2 sm:px-4">
                    Akcje
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {genres?.map((genre: GenreDto) => (
                  <TableRow key={genre.id}>
                    <TableCell className="font-medium text-xs sm:text-sm px-2 sm:px-4">
                      {genre.id}
                    </TableCell>
                    <TableCell className="text-xs sm:text-sm px-2 sm:px-4">{genre.name}</TableCell>
                    <TableCell className="text-right px-2 sm:px-4">
                      <AlertDialog>
                        <AlertDialogTrigger asChild>
                          <Button
                            variant="destructive"
                            size="icon"
                            className="h-8 w-8 sm:h-10 sm:w-10"
                            onClick={() => setDeletingGenreId(genre.id)}
                            disabled={isDeleting && deletingGenreId === genre.id}
                          >
                            {isDeleting && deletingGenreId === genre.id ? (
                              <Loader className="size-3 sm:size-4 animate-spin" />
                            ) : (
                              <Trash2 className="size-3 sm:size-4" />
                            )}
                          </Button>
                        </AlertDialogTrigger>
                        <AlertDialogContent className="max-w-[90vw] sm:max-w-lg">
                          <AlertDialogHeader>
                            <AlertDialogTitle className="text-sm sm:text-base">
                              Czy na pewno chcesz usunąć ten gatunek?
                            </AlertDialogTitle>
                            <AlertDialogDescription className="text-xs sm:text-sm">
                              Tej operacji nie można cofnąć. Spowoduje to trwałe usunięcie gatunku z
                              bazy danych.
                            </AlertDialogDescription>
                          </AlertDialogHeader>
                          <AlertDialogFooter className="flex-col sm:flex-row gap-2">
                            <AlertDialogCancel className="text-xs sm:text-sm m-0">
                              Anuluj
                            </AlertDialogCancel>
                            <AlertDialogAction
                              onClick={() => handleDelete(genre.id)}
                              className="text-xs sm:text-sm m-0"
                            >
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
          <h1 className="text-base sm:text-lg font-semibold md:text-2xl">Zarządzanie gatunkami</h1>
          <div className="border rounded-lg p-3 sm:p-4">
            <h2 className="text-sm sm:text-md font-semibold mb-3 sm:mb-4">Dodaj nowy gatunek</h2>
            <Form {...form}>
              <form
                onSubmit={form.handleSubmit(onSubmit)}
                className="flex flex-col sm:flex-row items-start gap-3 sm:gap-4"
              >
                <FormField
                  control={form.control}
                  name="name"
                  render={({ field }) => (
                    <FormItem className="flex-grow w-full sm:w-auto">
                      <FormLabel className="text-xs sm:text-sm">Nazwa gatunku</FormLabel>
                      <FormControl>
                        <Input
                          placeholder="np. Komedia romantyczna"
                          className="text-sm"
                          {...field}
                        />
                      </FormControl>
                      <FormMessage className="text-xs" />
                    </FormItem>
                  )}
                />
                <Button
                  type="submit"
                  disabled={isCreating}
                  className="mt-0 sm:mt-8 w-full sm:w-auto text-xs sm:text-sm h-9 sm:h-10"
                >
                  {isCreating ? (
                    <Loader className="mr-2 size-3 sm:size-4 animate-spin" />
                  ) : (
                    <PlusCircle className="mr-2 size-3 sm:size-4" />
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
