import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { toast } from "sonner";
import { axiosInstance } from "@/interceptor";

const declineReservation = async (reservationId: string) => {
  const { data } = await axiosInstance.post(
    `/reservations/${reservationId}/decline`
  );
  return data;
};

export const useDeclineReservation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: declineReservation,
    onSuccess: () => {
      toast.success("Rezerwacja została pomyślnie odrzucona.");
      queryClient.invalidateQueries({ queryKey: ["allReservations"] });
    },
    onError: (error) => {
      if (isAxiosError(error)) {
        const message =
          error.response?.status === 400
            ? "Nie można odrzucić tej rezerwacji (może już nie być oczekująca)."
            : "Wystąpił błąd podczas odrzucania rezerwacji.";
        toast.error(message);
      } else {
        toast.error("Wystąpił nieoczekiwany błąd.");
      }
      console.error("Błąd podczas odrzucania rezerwacji:", error);
    },
  });
};
