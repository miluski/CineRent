import { useQuery } from "@tanstack/react-query";
import { axiosInstance } from "@/interceptor";
import type { DvdDto } from "@/interfaces/responses/DvdDto";

const getRecommendations = async (): Promise<DvdDto[]> => {
  const response = await axiosInstance.get<DvdDto[]>("/user/recommendations");
  return response.data;
};

export const useGetRecommendations = () => {
  return useQuery({
    queryKey: ["recommendations"],
    queryFn: getRecommendations,
  });
};
