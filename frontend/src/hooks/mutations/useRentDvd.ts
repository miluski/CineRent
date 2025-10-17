import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { toast } from "sonner";
import { axiosInstance } from "@/interceptor";
import type { CreateReservationRequestDto } from "@/interfaces/requests/CreateReservationRequestDto";

const createReservation = async (
  reservationData: CreateReservationRequestDto
) => {
  const { data } = await axiosInstance.post(
    "/reservations/new",
    reservationData
  );
  return data;
};

export const useRentDvd = (onSuccessCallback?: () => void) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createReservation,
    onSuccess: (_, variables) => {
      toast.success("Rezerwacja została pomyślnie złożona!");
      queryClient.invalidateQueries({
        queryKey: ["dvd", String(variables.dvdId)],
      });
      queryClient.invalidateQueries({ queryKey: ["reservations"] });
      onSuccessCallback?.();
    },
    onError: (error) => {
      const message =
        isAxiosError(error) && error.response?.status === 400
          ? "Nie można zarezerwować. Sprawdź dostępność lub daty."
          : "Wystąpił błąd podczas składania rezerwacji.";
      toast.error(message);
      console.error("Błąd podczas tworzenia rezerwacji:", error);
    },
  });
};
