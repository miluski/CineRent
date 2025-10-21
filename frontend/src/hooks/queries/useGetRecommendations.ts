import { axiosInstance } from '@/interceptor';
import type { DvdDto } from '@/interfaces/responses/DvdDto';
import type { PagedResponseDto } from '@/interfaces/responses/PagedResponseDto';
import { useQuery } from '@tanstack/react-query';

interface GetRecommendationsParams {
  page?: number;
  size?: number;
}

const getRecommendations = async (
  params?: GetRecommendationsParams
): Promise<PagedResponseDto<DvdDto>> => {
  const response = await axiosInstance.get<PagedResponseDto<DvdDto>>('/user/recommendations', {
    params,
  });
  return response.data;
};

export const useGetRecommendations = (params?: GetRecommendationsParams) => {
  return useQuery({
    queryKey: ['recommendations', params],
    queryFn: () => getRecommendations(params),
  });
};
