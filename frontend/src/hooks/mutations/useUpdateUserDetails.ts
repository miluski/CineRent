import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { toast } from "sonner";
import type { UpdateUserDetailsRequestDto } from "@/interfaces/requests/UpdateUserDetailsRequestDto";
import type { UserDto } from "@/interfaces/responses/UserDto";
import { genres } from "@/utils/genres";
import { axiosInstance } from "@/interceptor";

export const useUpdateUserDetails = (
  onSuccessCallback?: () => void,
  onErrorCallback?: () => void
) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: UpdateUserDetailsRequestDto) => {
      const response = await axiosInstance.patch("/user/edit", data);
      return response.data;
    },
    onSuccess: (_, payload) => {
      queryClient.setQueryData(
        ["user", "me"],
        (oldUser: UserDto | undefined) => {
          if (!oldUser) return oldUser;

          const updatedUser: UserDto = { ...oldUser };
          if (payload.nickname !== undefined) {
            updatedUser.nickname = payload.nickname;
          }
          if (payload.age !== undefined) {
            updatedUser.age = payload.age;
          }
          if (payload.preferredGenresIdentifiers !== undefined) {
            updatedUser.preferredGenres = payload.preferredGenresIdentifiers
              .map((id) => genres.find((g) => g.id === id)?.label)
              .filter(Boolean) as string[];
          }
          return updatedUser;
        }
      );
      toast.success("Profil zaktualizowano pomyślnie!");
      onSuccessCallback?.();
    },
    onError: (error) => {
      if (isAxiosError(error) && error.response?.status === 422) {
        toast.error("Błąd walidacji: Sprawdź wprowadzone dane.");
      } else {
        toast.error("Wystąpił błąd podczas aktualizacji profilu.");
      }
      console.error("Update user failed:", error);
      onErrorCallback?.();
    },
  });
};
