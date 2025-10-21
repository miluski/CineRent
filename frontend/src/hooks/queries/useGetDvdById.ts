import { useQuery } from "@tanstack/react-query";
import type { DvdDto } from "@/interfaces/responses/DvdDto";
import { axiosInstance } from "@/interceptor";

const getDvdById = async (dvdId: string): Promise<DvdDto> => {
  const { data } = await axiosInstance.get<DvdDto>(`/dvd/${dvdId}`);
  return data;
};


export const useGetDvdById = (dvdId: string) => {
  return useQuery({
    queryKey: ["dvd", dvdId],
    queryFn: () => getDvdById(dvdId),
    enabled: !!dvdId,
  });
};
