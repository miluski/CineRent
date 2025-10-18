import { useMutation, useQueryClient } from "@tanstack/react-query";

import type { GenreDto } from "@/interfaces/responses/GenreDto";
import { axiosInstance } from "@/interceptor";

const deleteGenre = async (genreId: string) => {
  const response = await axiosInstance.delete(`/genres/${genreId}/delete`);
  return response.data;
};

export const useDeleteGenre = () => {
  const queryClient = useQueryClient();

  return useMutation<unknown, Error, string>({
    mutationFn: deleteGenre,
    onSuccess: (_, genreId) => {
      queryClient.setQueryData<GenreDto[]>(["genres"], (oldData) => {
        return oldData
          ? oldData.filter((genre) => genre.id !== Number(genreId))
          : [];
      });
      queryClient.invalidateQueries({ queryKey: ["genres"] });
    },
  });
};
