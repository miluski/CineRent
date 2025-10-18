import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import { DashboardPage } from "./pages/DashboardPage";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { AuthProvider, useAuth } from "./contexts/AuthContext";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { Spinner } from "./components/ui/spinner";
import { ProfilePage } from "./pages/ProfilePage";
import { Toaster } from "sonner";
import { AddDvdPage } from "./pages/AddDvdPage";
import { EditDvdPage } from "./pages/EditDvdPage";
import { RentDvdPage } from "./pages/RentDvdPage";
import { UserReservationsPage } from "./pages/UserReservationsPage";
import { RentalsPage } from "./pages/RentalsPage";
import { TransactionsHistoryPage } from "./pages/TransactionsHistoryPage";
import { ReservationManagementPage } from "./pages/ReservationManagementPage";
import { RecommendationsPage } from "./pages/RecommendationsPage";
import { ReturnRequestsPage } from "./pages/ReturnRequestsPage";

const queryClient = new QueryClient();

const AppRoutes = () => {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Spinner className="size-10 text-primary" />
      </div>
    );
  }

  return (
    <Routes>
      <Route
        path="/"
        element={user ? <Navigate to="/dashboard" /> : <Navigate to="/login" />}
      />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/dvd/:id" element={<RentDvdPage />} />
        <Route path="/reservations" element={<UserReservationsPage />} />
        <Route path="/rentals" element={<RentalsPage />} />
        <Route path="/recommendations" element={<RecommendationsPage />} />
        <Route
          path="/transactions-history"
          element={<TransactionsHistoryPage />}
        />
      </Route>

      <Route element={<ProtectedRoute adminOnly />}>
        <Route path="/admin/dvd/create" element={<AddDvdPage />} />
        <Route path="/admin/dvd/edit/:id" element={<EditDvdPage />} />
        <Route
          path="/admin/reservations"
          element={<ReservationManagementPage />}
        />
        <Route path="/admin/returns" element={<ReturnRequestsPage />} />
      </Route>
    </Routes>
  );
};

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Router>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </Router>
      <Toaster />
    </QueryClientProvider>
  );
}

export default App;
