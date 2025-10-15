import { Button } from "@/components/ui/button";
import { useLogout } from "@/hooks/auth/logout.mutation";
import { useNavigate } from "react-router-dom";

export function DashboardPage() {
  const navigate = useNavigate();
  const logoutMutation = useLogout();

  const handleLogout = () => {
    logoutMutation.mutate(undefined, {
      onSuccess: () => {
        navigate("/");
      },
      onError: (error) => {
        console.error("Logout failed", error);
        // Nawet jeśli wylogowanie na serwerze się nie powiedzie,
        // przekierowujemy użytkownika. Interceptor powinien sobie poradzić
        // z wygasłym tokenem przy następnej próbie.
        navigate("/");
      },
    });
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen">
      <h1 className="text-3xl font-bold mb-4">Witaj w CineRent!</h1>
      <p className="mb-8">To jest Twój panel główny.</p>
      <Button onClick={handleLogout} disabled={logoutMutation.isPending}>
        {logoutMutation.isPending ? "Wylogowywanie..." : "Wyloguj"}
      </Button>
    </div>
  );
}
