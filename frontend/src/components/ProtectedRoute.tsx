import { useAuth } from "@/contexts/AuthContext";
import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { Spinner } from "./ui/spinner";

interface ProtectedRouteProps {
  adminOnly?: boolean;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  adminOnly = false,
}) => {
  const { isAdmin, isLoading, isAdminStatusLoading } = useAuth();

  if (isLoading || isAdminStatusLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Spinner className="size-10 text-primary" />
      </div>
    );
  }

  if (adminOnly && !isAdmin && !isAdminStatusLoading) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};
