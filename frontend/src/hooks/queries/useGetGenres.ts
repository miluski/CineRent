import { useQuery } from "@tanstack/react-query";
import { axiosInstance } from "@/interceptor";
import type { GenreDto } from "@/interfaces/responses/GenreDto";

const getGenres = async (): Promise<GenreDto[]> => {
  try {
    const response = await axiosInstance.get<GenreDto[]>("/genres");
    return response.data;
  } catch (error) {
    console.error("Error fetching genres", error);
    throw new Error("Nie udało się pobrać listy gatunków.");
  }
};

export const useGetGenres = () => {
  return useQuery<GenreDto[], Error>({
    queryKey: ["genres"],
    queryFn: getGenres,
  });
};
