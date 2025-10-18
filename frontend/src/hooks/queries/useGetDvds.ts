import { useQuery } from "@tanstack/react-query";
import type { DvdDto } from "@/interfaces/responses/DvdDto";
import { axiosInstance } from "@/interceptor";

interface GetDvdsParams {
  "search-phrase"?: string;
  "genres-names"?: string[];
  "genres-ids"?: number[];
}

const getDvds = async (params?: GetDvdsParams): Promise<DvdDto[]> => {
  const { data } = await axiosInstance.get<DvdDto[]>("/dvd", {
    params,
  });
  console.log("data", data);

  return data;
};

export const useGetDvds = (params?: GetDvdsParams) => {
  return useQuery({
    queryKey: ["dvds", params],
    queryFn: () => getDvds(params),
  });
};
