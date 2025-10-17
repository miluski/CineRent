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
import { useNavigate } from "react-router-dom";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Camera } from "lucide-react";
import { DashboardSidebar } from "@/components/DashboardSidebar";
import { DashboardHeader } from "@/components/DashboardHeader";
import { genres } from "@/utils/genres";
import { useAuth } from "@/contexts/AuthContext";
import { useUpdateUserDetails } from "@/hooks/mutations/useUpdateUserDetails";
import { toast } from "sonner";
import type { UpdateUserDetailsRequestDto } from "@/interfaces/requests/UpdateUserDetailsRequestDto";

const formSchema = z.object({
  nickname: z
    .string()
    .min(3, "Nazwa użytkownika musi mieć co najmniej 3 znaki.")
    .max(50, "Nazwa użytkownika nie może być dłuższa niż 50 znaków."),
  password: z
    .string()
    .min(8, "Hasło musi mieć co najmniej 8 znaków.")
    .max(100, "Hasło nie może być dłuższe niż 100 znaków.")
    .optional()
    .or(z.literal("")),
  age: z.coerce
    .number<number>()
    .min(18, "Wiek musi być większy od 18.")
    .max(149, "Wiek nie może być większy niż 149."),
  preferredGenresIdentifiers: z.array(z.number()).optional(),
});

export function ProfilePage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const updateUserMutation = useUpdateUserDetails(() => {
    form.reset({ ...form.getValues(), password: "" });
  });

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      nickname: user?.nickname ?? "",
      password: "",
      age: user?.age ?? 18,
      preferredGenresIdentifiers:
        (user?.preferredGenres
          ?.map((genreName) => genres.find((g) => g.label === genreName)?.id)
          .filter(Boolean) as number[]) ?? [],
    },
  });

  async function onSubmit(values: z.infer<typeof formSchema>) {
    const payload: UpdateUserDetailsRequestDto = {};

    if (values.nickname !== user?.nickname) {
      payload.nickname = values.nickname;
    }

    if (values.password && values.password !== "") {
      payload.password = values.password;
    }

    if (values.age !== user?.age) {
      payload.age = values.age;
    }

    const currentGenreIds =
      user?.preferredGenres
        ?.map((genreName) => genres.find((g) => g.label === genreName)?.id)
        .filter(Boolean)
        .sort() ?? [];
    const newGenreIds = (values.preferredGenresIdentifiers ?? []).sort();

    if (JSON.stringify(currentGenreIds) !== JSON.stringify(newGenreIds)) {
      payload.preferredGenresIdentifiers = newGenreIds;
    }

    if (Object.keys(payload).length === 0) {
      toast.info("Nie wprowadzono żadnych zmian.");
      return;
    }

    updateUserMutation.mutate(payload);
  }

  return (
    <div className="grid min-h-screen w-full md:grid-cols-[220px_1fr] lg:grid-cols-[280px_1fr]">
      <DashboardSidebar />
      <div className="flex flex-col">
        <DashboardHeader />
        <main className="flex flex-1 flex-col gap-4 p-4 lg:gap-6 lg:p-6">
          <div className="flex items-center animate-in fade-in-0 slide-in-from-bottom-4 duration-700 fill-mode-backwards">
            <h1 className="text-lg font-semibold md:text-2xl">
              Informacje o koncie
            </h1>
          </div>
          <Card
            className={
              "animate-in fade-in-0 slide-in-from-bottom-4 duration-700 delay-100 fill-mode-backwards"
            }
          >
            <CardHeader>
              <CardTitle>Edytuj swój profil</CardTitle>
              <CardDescription>
                Zmień swoje dane i preferencje. Kliknij "Zakończ edycję", aby
                zapisać.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Form {...form}>
                <form
                  onSubmit={form.handleSubmit(onSubmit)}
                  className="grid grid-cols-1 gap-8 md:grid-cols-3"
                >
                  <div className="flex flex-col items-center gap-4 md:col-span-1">
                    <Avatar className="h-40 w-40">
                      <AvatarImage
                        draggable="false"
                        src="https://github.com/shadcn.png"
                      />
                      <AvatarFallback>
                        {user?.nickname?.substring(0, 2).toUpperCase()}
                      </AvatarFallback>
                    </Avatar>
                    <p className="font-semibold">{user?.nickname}</p>
                    <Button variant="outline" className="w-full">
                      <Camera className="mr-2 h-4 w-4" />
                      Zmień profilowe
                    </Button>
                  </div>

                  <div className="space-y-4 md:col-span-2">
                    <FormField
                      control={form.control}
                      name="nickname"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Nickname</FormLabel>
                          <FormControl>
                            <Input {...field} />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                    <FormField
                      control={form.control}
                      name="password"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Nowe hasło (opcjonalnie)</FormLabel>
                          <FormControl>
                            <Input
                              type="password"
                              placeholder="Zostaw puste, jeśli bez zmian"
                              {...field}
                            />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                    <FormField
                      control={form.control}
                      name="age"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Wiek</FormLabel>
                          <FormControl>
                            <Input type="number" {...field} />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                    <div>
                      <FormLabel>Edytuj preferencje filmowe</FormLabel>
                      <div className="mt-2 grid grid-cols-2 gap-2 sm:grid-cols-3 md:grid-cols-4">
                        {genres.map((genre) => (
                          <FormField
                            key={genre.id}
                            control={form.control}
                            name="preferredGenresIdentifiers"
                            render={({ field }) => (
                              <FormItem className="flex flex-row items-start space-x-3 space-y-0">
                                <FormControl>
                                  <Checkbox
                                    checked={field.value?.includes(genre.id)}
                                    onCheckedChange={(checked) => {
                                      return checked
                                        ? field.onChange([
                                            ...(field.value ?? []),
                                            genre.id,
                                          ])
                                        : field.onChange(
                                            field.value?.filter(
                                              (value) => value !== genre.id
                                            )
                                          );
                                    }}
                                  />
                                </FormControl>
                                <FormLabel className="font-normal">
                                  {genre.label}
                                </FormLabel>
                              </FormItem>
                            )}
                          />
                        ))}
                      </div>
                    </div>
                  </div>

                  <div className="flex flex-col gap-2 md:col-span-3 md:flex-row md:justify-end">
                    <Button
                      type="submit"
                      disabled={updateUserMutation.isPending}
                    >
                      {updateUserMutation.isPending
                        ? "Zapisywanie..."
                        : "Zakończ edycję danych"}
                    </Button>
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => navigate(-1)}
                    >
                      Powrót
                    </Button>
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
