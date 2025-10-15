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
import { useLogin } from "@/hooks/auth/login.mutation";
import { useNavigate } from "react-router-dom";

const formSchema = z.object({
  nickname: z.string().min(3, "Nazwa użytkownika jest za krótka."),
  password: z.string().min(8, "Hasło jest za krótkie."),
});

export function LoginPage() {
  const navigate = useNavigate();
  const loginMutation = useLogin();

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      nickname: "",
      password: "",
    },
  });

  function onSubmit(values: z.infer<typeof formSchema>) {
    loginMutation.mutate(values, {
      onSuccess: () => {
        navigate("/dashboard");
      },
      onError: (error) => {
        console.error("Login failed", error);
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
          <h1 className="text-2xl font-bold flex items-center">Movie Rent</h1>
        </div>

        {/* Tagline at bottom */}
        <div className="mt-auto mb-24">
          <h2 className="text-2xl md:text-4xl font-bold leading-snug">
            "Twoje wszystkie filmy, które szukasz bez szukania."
          </h2>
        </div>
      </div>

      {/* Right section - Login form */}
      <div className="w-full md:w-1/2 bg-white p-8 flex items-center justify-center">
        <div className="w-full max-w-md space-y-6">
          <div className="text-center">
            <h2 className="text-3xl font-bold mb-2">Zaloguj się</h2>
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

              <Button
                type="submit"
                className="w-full bg-black hover:bg-gray-800 text-white"
                disabled={loginMutation.isPending}
              >
                {loginMutation.isPending ? "Logowanie..." : "Zaloguj się"}
              </Button>
            </form>
          </Form>

          {/* Divider */}
          <div className="relative flex items-center">
            <div className="flex-grow border-t border-gray-300"></div>
            <span className="flex-shrink mx-4 text-gray-500 text-sm">lub</span>
            <div className="flex-grow border-t border-gray-300"></div>
          </div>

          {/* Create account button */}
          <Link to="/register" className="block w-full">
            <Button variant="outline" className="w-full">
              Utwórz konto
            </Button>
          </Link>
        </div>
      </div>
    </div>
  );
}
