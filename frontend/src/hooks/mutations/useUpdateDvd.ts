import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { toast } from "sonner";
import type { DvdDto } from "@/interfaces/responses/DvdDto";
import { axiosInstance } from "@/interceptor";

interface UpdateDvdPayload {
  id: string;
  dvdData: Partial<DvdDto>;
}

export const useUpdateDvd = (
  onSuccessCallback?: () => void,
  onErrorCallback?: () => void
) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, dvdData }: UpdateDvdPayload) => {
      const response = await axiosInstance.patch(`/dvd/${id}/edit`, dvdData);
      return response.data;
    },
    onSuccess: (_, { id }) => {
      toast.success("Film zaktualizowano pomyślnie!");
      queryClient.invalidateQueries({ queryKey: ["dvds"] });
      queryClient.invalidateQueries({ queryKey: ["dvd", id] });
      onSuccessCallback?.();
    },
    onError: (error) => {
      if (isAxiosError(error) && error.response?.status === 422) {
        toast.error("Błąd walidacji: Sprawdź wprowadzone dane.");
      } else {
        toast.error("Wystąpił błąd podczas aktualizacji filmu.");
      }
      console.error("Update DVD failed:", error);
      onErrorCallback?.();
    },
  });
};
