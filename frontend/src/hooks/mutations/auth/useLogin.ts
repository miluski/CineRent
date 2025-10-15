import { axiosInstance } from "@/interceptor";
import { useMutation } from "@tanstack/react-query";

export const useLogin = () => {
  return useMutation({
    mutationFn: async (data: { nickname: string; password: string }) => {
      const response = await axiosInstance.post("/auth/login", data);
      return response.data;
    },
  });
};
