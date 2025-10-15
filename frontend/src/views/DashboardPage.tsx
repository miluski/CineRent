import { Button } from "@/components/ui/button";
import { useAuth } from "@/contexts/AuthContext";

export function DashboardPage() {
  const { logout } = useAuth();

  return (
    <div className="flex flex-col items-center justify-center min-h-screen">
      <h1 className="text-3xl font-bold mb-4">Witaj w CineRent!</h1>
      <p className="mb-8">To jest Twój panel główny.</p>
      <Button onClick={logout}>Wyloguj</Button>
    </div>
  );
}
