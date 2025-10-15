import { axiosInstance } from "@/interceptor";
import { useMutation } from "@tanstack/react-query";

interface RegisterData {
  nickname: string;
  password: string;
  age: number;
}

export const useRegister = () => {
  return useMutation({
    mutationFn: async (data: RegisterData) => {
      const response = await axiosInstance.post("/auth/register", data);
      return response.data;
    },
  });
};
