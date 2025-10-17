import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { toast } from "sonner";
import { axiosInstance } from "@/interceptor";

const acceptReservation = async (reservationId: string) => {
  const { data } = await axiosInstance.post(
    `/reservations/${reservationId}/accept`
  );
  return data;
};

export const useAcceptReservation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: acceptReservation,
    onSuccess: () => {
      toast.success("Rezerwacja została pomyślnie zaakceptowana!");
      queryClient.invalidateQueries({ queryKey: ["allReservations"] });
    },
    onError: (error) => {
      if (isAxiosError(error)) {
        const message =
          error.response?.status === 400
            ? "Nie można zaakceptować tej rezerwacji (może już nie być oczekująca)."
            : "Wystąpił błąd podczas akceptowania rezerwacji.";
        toast.error(message);
      } else {
        toast.error("Wystąpił nieoczekiwany błąd.");
      }
      console.error("Błąd podczas akceptowania rezerwacji:", error);
    },
  });
};
