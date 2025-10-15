import { axiosInstance } from "@/interceptor";
import { useMutation } from "@tanstack/react-query";

export const useLogout = () => {
  return useMutation({
    mutationFn: async () => {
      const response = await axiosInstance.post("/auth/logout");
      return response.data;
    },
  });
};
