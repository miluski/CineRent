import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { toast } from "sonner";
import { axiosInstance } from "@/interceptor";

const returnRequest = async (rentalId: string) => {
  const { data } = await axiosInstance.post(
    `/rentals/${rentalId}/return-request`
  );
  return data;
};

export const useReturnRequest = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: returnRequest,
    onSuccess: () => {
      toast.success("Prośba o zwrot została pomyślnie wysłana!");
      queryClient.invalidateQueries({ queryKey: ["rentals"] });
      queryClient.invalidateQueries({ queryKey: ["returnRequests"] });
    },
    onError: (error) => {
      if (isAxiosError(error)) {
        const status = error.response?.status;
        let message = "Wystąpił błąd podczas wysyłania prośby o zwrot.";

        if (status === 400) {
          message = "Nie można złożyć prośby o zwrot dla tego wypożyczenia.";
        } else if (status === 404) {
          message = "Nie znaleziono wypożyczenia.";
        }

        toast.error(message);
      } else {
        toast.error("Wystąpił nieoczekiwany błąd.");
      }
      console.error("Błąd podczas wysyłania prośby o zwrot:", error);
    },
  });
};
