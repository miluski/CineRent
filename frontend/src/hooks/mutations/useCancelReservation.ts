import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { toast } from "sonner";
import { axiosInstance } from "@/interceptor";

const cancelReservation = async (reservationId: string) => {
  const { data } = await axiosInstance.post(
    `/reservations/${reservationId}/cancel`
  );
  return data;
};

export const useCancelReservation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: cancelReservation,
    onSuccess: () => {
      toast.success("Rezerwacja została pomyślnie anulowana!");
      queryClient.invalidateQueries({ queryKey: ["userReservations"] });
    },
    onError: (error) => {
      if (isAxiosError(error)) {
        const status = error.response?.status;
        switch (status) {
          case 400:
            toast.error(
              "Nieprawidłowe ID rezerwacji lub rezerwacja nie jest w stanie oczekiwania"
            );
            break;
          case 403:
            toast.error("Możesz anulować tylko swoje rezerwacje");
            break;
          case 404:
            toast.error("Nie znaleziono rezerwacji");
            break;
          default:
            toast.error("Nie udało się anulować rezerwacji. Spróbuj ponownie.");
        }
      } else {
        toast.error("Wystąpił nieoczekiwany błąd");
      }
    },
  });
};
