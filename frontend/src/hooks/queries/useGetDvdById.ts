import { useQuery } from "@tanstack/react-query";
import type { DvdDto } from "@/interfaces/responses/DvdDto";
import { axiosInstance } from "@/interceptor";

const getDvdById = async (dvdId: string): Promise<DvdDto> => {
  const { data } = await axiosInstance.get<DvdDto>(`/dvd/${dvdId}`);
  return data;
};

/**
 * React Query hook to get a single DVD by its ID.
 *
 * @param dvdId - The ID of the DVD to fetch.
 * @returns The result of the query, providing the DVD data, loading state, and error information.
 */
export const useGetDvdById = (dvdId: string) => {
  return useQuery({
    queryKey: ["dvd", dvdId],
    queryFn: () => getDvdById(dvdId),
    enabled: !!dvdId,
  });
};
