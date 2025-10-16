import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { useRegister } from "@/hooks/mutations/auth/useRegister";
import { useNavigate } from "react-router-dom";
import { Checkbox } from "@/components/ui/checkbox";

const genres: { id: number; label: string }[] = [
  { id: 1, label: "Science-Fiction" },
  { id: 2, label: "Akcja" },
  { id: 3, label: "Dramat" },
  { id: 4, label: "Horror" },
  { id: 5, label: "Thriller" },
];

const formSchema = z.object({
  nickname: z
    .string()
    .min(3, "Nazwa użytkownika musi mieć co najmniej 3 znaki.")
    .max(50, "Nazwa użytkownika nie może być dłuższa niż 50 znaków."),
  password: z
    .string()
    .min(8, "Hasło musi mieć co najmniej 8 znaków.")
    .max(100, "Hasło nie może być dłuższe niż 100 znaków."),
  age: z.coerce
    .number<number>()
    .min(18, "Wiek musi być większy od 18.")
    .max(149, "Wiek nie może być większy niż 149."),
  preferredGenresIdentifiers: z.array(z.number()).optional(),
});

export function RegisterPage() {
  const registerMutation = useRegister();
  const navigate = useNavigate();

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      nickname: "",
      password: "",
      age: 18,
      preferredGenresIdentifiers: [],
    },
  });

  function onSubmit(values: z.infer<typeof formSchema>) {
    registerMutation.mutate(values, {
      onSuccess: () => {
        navigate("/login");
      },
      onError: (error) => {
        console.error("Registration failed", error);
      },
    });
  }

  return (
    <div className="flex flex-col md:flex-row min-h-screen">
      {/* Left section - Background image with branding */}
      <div
        className="relative w-full md:w-1/2 bg-slate-900 text-white p-8 flex flex-col bg-cover bg-center"
        style={{
          backgroundImage:
            "linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.7)), url('/auth-overlay.jpg')",
        }}
      >
        {/* Logo */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold flex items-center animate-pulse select-none">
            <span className="text-red-500">O</span>pasRent
          </h1>
        </div>

        {/* Tagline at bottom */}
        <div className="mt-auto mb-24">
          <h2 className="text-2xl md:text-4xl font-bold leading-snug">
            "Twoje wszystkie filmy, które szukasz bez szukania."
          </h2>
        </div>
      </div>

      {/* Right section - Registration form */}
      <div className="w-full md:w-1/2 bg-white p-8 flex items-center justify-center">
        <div className="w-full max-w-md space-y-6">
          <div className="text-center">
            <h2 className="text-3xl font-bold mb-2">Zarejestruj się</h2>
            <p className="text-gray-600 text-sm">
              Jedno kliknięcie - i już jesteś w filmowym świecie
            </p>
          </div>

          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="nickname"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Nickname</FormLabel>
                    <FormControl>
                      <Input {...field} className="w-full" />
                    </FormControl>
                    <FormMessage className="h-5" />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Hasło</FormLabel>
                    <FormControl>
                      <Input type="password" {...field} className="w-full" />
                    </FormControl>
                    <FormMessage className="h-5" />
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
                      <Input
                        type="number"
                        {...field}
                        min={18}
                        className="w-full"
                        onChange={(e) => {
                          field.onChange(e.target.valueAsNumber);
                        }}
                      />
                    </FormControl>
                    <FormMessage className="h-5" />
                  </FormItem>
                )}
              />

              <div className="space-y-2">
                <FormLabel>Preferencje filmowe</FormLabel>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
                  {genres.map((genre) => (
                    <div key={genre.id} className="flex items-center space-x-2">
                      <Checkbox
                        id={`genre-${genre.id}`}
                        onCheckedChange={(checked) => {
                          const currentValues =
                            form.getValues().preferredGenresIdentifiers || [];

                          const newValues = checked
                            ? [...currentValues, genre.id]
                            : currentValues.filter((id) => id !== genre.id);

                          form.setValue(
                            "preferredGenresIdentifiers",
                            newValues
                          );
                        }}
                      />
                      <label
                        htmlFor={`genre-${genre.id}`}
                        className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                      >
                        {genre.label}
                      </label>
                    </div>
                  ))}
                </div>
              </div>

              <Button
                type="submit"
                className="w-full bg-black hover:bg-gray-800 text-white"
                disabled={registerMutation.isPending}
              >
                {registerMutation.isPending
                  ? "Rejestrowanie..."
                  : "Utwórz konto"}
              </Button>
            </form>
          </Form>

          {/* Divider */}
          <div className="relative flex items-center">
            <div className="flex-grow border-t border-gray-300"></div>
            <span className="flex-shrink mx-4 text-gray-500 text-sm">lub</span>
            <div className="flex-grow border-t border-gray-300"></div>
          </div>

          {/* Login button */}
          <Link to="/login" className="block w-full">
            <Button
              variant="outline"
              className="w-full text-black border-black"
            >
              Zaloguj się
            </Button>
          </Link>
        </div>
      </div>
    </div>
  );
}
