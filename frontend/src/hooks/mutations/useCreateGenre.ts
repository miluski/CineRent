import { useMutation, useQueryClient } from "@tanstack/react-query";

import { axiosInstance } from "@/interceptor";
import type { GenreCreateDto } from "@/interfaces/requests/GenreCreateDto";
import type { GenreDto } from "@/interfaces/responses/GenreDto";

const createGenre = async (genreData: GenreCreateDto): Promise<GenreDto> => {
  const response = await axiosInstance.post<GenreDto>(
    "/genres/create",
    genreData
  );
  return response.data;
};

export const useCreateGenre = () => {
  const queryClient = useQueryClient();

  return useMutation<GenreDto, Error, GenreCreateDto>({
    mutationFn: createGenre,
    onSuccess: (newGenre) => {
      queryClient.setQueryData<GenreDto[]>(["genres"], (oldData) => {
        return oldData ? [...oldData, newGenre] : [newGenre];
      });
      queryClient.invalidateQueries({ queryKey: ["genres"] });
    },
  });
};
