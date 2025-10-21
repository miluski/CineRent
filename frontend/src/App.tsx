import { ProtectedRoute } from '@/components/ProtectedRoute';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Navigate, Route, BrowserRouter as Router, Routes } from 'react-router-dom';
import { Toaster } from 'sonner';
import { Spinner } from './components/ui/spinner';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { AddDvdPage } from './pages/AddDvdPage';
import { DashboardPage } from './pages/DashboardPage';
import { EditDvdPage } from './pages/EditDvdPage';
import { GenresManagementPage } from './pages/GenresManagementPage';
import { LoginPage } from './pages/LoginPage';
import { ProfilePage } from './pages/ProfilePage';
import { RecommendationsPage } from './pages/RecommendationsPage';
import { RegisterPage } from './pages/RegisterPage';
import { RentalsPage } from './pages/RentalsPage';
import { RentDvdPage } from './pages/RentDvdPage';
import { ReservationManagementPage } from './pages/ReservationManagementPage';
import { ReturnRequestsPage } from './pages/ReturnRequestsPage';
import { TransactionsHistoryPage } from './pages/TransactionsHistoryPage';
import { UserReservationsPage } from './pages/UserReservationsPage';

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
      <Route path="/" element={user ? <Navigate to="/dashboard" /> : <Navigate to="/login" />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/dvd/:id" element={<RentDvdPage />} />
        <Route path="/reservations" element={<UserReservationsPage />} />
        <Route path="/rentals" element={<RentalsPage />} />
        <Route path="/recommendations" element={<RecommendationsPage />} />
        <Route path="/transactions-history" element={<TransactionsHistoryPage />} />
      </Route>

      <Route element={<ProtectedRoute adminOnly />}>
        <Route path="/admin/dvd/create" element={<AddDvdPage />} />
        <Route path="/admin/dvd/edit/:id" element={<EditDvdPage />} />
        <Route path="/admin/reservations" element={<ReservationManagementPage />} />
        <Route path="/admin/returns" element={<ReturnRequestsPage />} />
        <Route path="/admin/genres" element={<GenresManagementPage />} />
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
      <Toaster position="top-center" richColors closeButton expand={false} />
    </QueryClientProvider>
  );
}

export default App;
