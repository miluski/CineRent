import { useMutation, useQueryClient } from "@tanstack/react-query";
import { axiosInstance } from "@/interceptor";
import { toast } from "sonner";
import type { CreateDvdRequestDto } from "@/interfaces/requests/CreateDvdRequestDto";

const createDvd = async (dvdData: CreateDvdRequestDto) => {
  const { data } = await axiosInstance.post("/dvd/create", dvdData);
  return data;
};

export const useCreateDvd = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createDvd,
    onSuccess: () => {
      toast.success("Film został pomyślnie dodany");
      queryClient.invalidateQueries({ queryKey: ["dvds"] });
    },
    onError: (error) => {
      console.error("Błąd podczas tworzenia DVD:", error);
      toast.error("Wystąpił błąd podczas dodawania filmu. Spróbuj ponownie.");
    },
  });
};
