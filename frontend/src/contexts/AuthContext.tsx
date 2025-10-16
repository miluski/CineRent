import { useLogin } from "@/hooks/mutations/auth/useLogin";
import { useLogout } from "@/hooks/mutations/auth/useLogout";
import { useGetUser } from "@/hooks/queries/useGetUser";
import { axiosInstance } from "@/interceptor";
import type { LoginRequestDto } from "@/interfaces/requests/LoginRequestDto";
import type { UserDto } from "@/interfaces/responses/UserDto";
import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import { useNavigate } from "react-router-dom";

interface AuthContextType {
  isAdmin: boolean;
  isLoading: boolean;
  login: (values: LoginRequestDto) => Promise<void>;
  logout: () => void;
  user: UserDto | undefined;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [isAdmin, setIsAdmin] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  const navigate = useNavigate();
  const loginMutation = useLogin();
  const logoutMutation = useLogout();
  const {
    data: user,
    isError,
    isSuccess,
    isLoading: isUserLoading,
  } = useGetUser();

  const checkAdminStatus = useCallback(async () => {
    try {
      await axiosInstance.get("/transactions/all");
      setIsAdmin(true);
    } catch (error) {
      console.error("Admin check failed", error);
      setIsAdmin(false);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (isSuccess && user) {
      checkAdminStatus();
    } else if (isError) {
      setIsAdmin(false);
      setIsLoading(false);
    } else if (!isSuccess && !isError) {
      setIsLoading(true);
    }
  }, [isError, isSuccess, user, checkAdminStatus]);

  const login = async (values: LoginRequestDto) => {
    try {
      await loginMutation.mutateAsync(values);
    } catch (error) {
      console.error("Login failed", error);
      throw error;
    }
  };

  const logout = () => {
    logoutMutation.mutate(undefined, {
      onSuccess: () => {
        setIsAdmin(false);
        navigate("/login");
      },
      onError: (error) => {
        console.error("Logout failed, clearing session locally.", error);
        setIsAdmin(false);
        navigate("/login");
      },
    });
  };

  const value = {
    isAdmin,
    isLoading: isLoading || isUserLoading,
    login,
    logout,
    user,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
