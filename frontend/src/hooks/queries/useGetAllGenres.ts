import { useQuery } from "@tanstack/react-query";

import { axiosInstance } from "@/interceptor";
import type { GenreDto } from "@/interfaces/responses/GenreDto";

const getAllGenres = async (): Promise<GenreDto[]> => {
  const response = await axiosInstance.get<GenreDto[]>("/genres");
  return response.data;
};

export const useGetAllGenres = () => {
  return useQuery({
    queryKey: ["genres"],
    queryFn: getAllGenres,
  });
};
