import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { toast } from "sonner";
import { axiosInstance } from "@/interceptor";

const acceptReturnRequest = async (rentalId: string) => {
  const { data } = await axiosInstance.post(
    `/rentals/${rentalId}/return-accept`
  );
  return data;
};

export const useAcceptReturnRequest = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: acceptReturnRequest,
    onSuccess: () => {
      toast.success(
        "Zwrot został pomyślnie zaakceptowany! Transakcja została utworzona."
      );
      queryClient.invalidateQueries({ queryKey: ["allReturnRequests"] });
      queryClient.invalidateQueries({ queryKey: ["rentals"] });
    },
    onError: (error) => {
      if (isAxiosError(error)) {
        const status = error.response?.status;
        let message = "Wystąpił błąd podczas akceptowania zwrotu.";

        if (status === 400) {
          message =
            "Nie można zaakceptować tego zwrotu (niewłaściwy status lub ID).";
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
      console.error("Błąd podczas akceptowania zwrotu:", error);
    },
  });
};
