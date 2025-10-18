import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { toast } from "sonner";
import { axiosInstance } from "@/interceptor";

const declineReturnRequest = async (rentalId: string) => {
  const { data } = await axiosInstance.post(
    `/rentals/${rentalId}/return-decline`
  );
  return data;
};

export const useDeclineReturnRequest = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: declineReturnRequest,
    onSuccess: () => {
      toast.success(
        "Prośba o zwrot została odrzucona. Wypożyczenie pozostaje aktywne."
      );
      queryClient.invalidateQueries({ queryKey: ["allReturnRequests"] });
      queryClient.invalidateQueries({ queryKey: ["rentals"] });
    },
    onError: (error) => {
      if (isAxiosError(error)) {
        const status = error.response?.status;
        let message = "Wystąpił błąd podczas odrzucania prośby o zwrot.";

        if (status === 400) {
          message =
            "Nie można odrzucić tej prośby o zwrot (niewłaściwy status lub ID).";
        } else if (status === 401) {
          message = "Brak autoryzacji. Zaloguj się ponownie.";
        } else if (status === 403) {
          message = "Brak uprawnień. Wymagane uprawnienia administratora.";
        } else if (status === 404) {
          message = "Nie znaleziono wypożyczenia.";
        }

        toast.error(message);
      } else {
        toast.error("Wystąpił nieoczekiwany błąd.");
      }
      console.error("Błąd podczas odrzucania prośby o zwrot:", error);
    },
  });
};
