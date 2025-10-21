import { axiosInstance } from '@/interceptor';
import type { DvdDto } from '@/interfaces/responses/DvdDto';
import type { PagedResponseDto } from '@/interfaces/responses/PagedResponseDto';
import { useQuery } from '@tanstack/react-query';

interface GetDvdsParams {
  'search-phrase'?: string;
  'genres-names'?: string[];
  'genres-ids'?: number[];
  page?: number;
  size?: number;
}

const getDvds = async (params?: GetDvdsParams): Promise<PagedResponseDto<DvdDto>> => {
  const { data } = await axiosInstance.get<PagedResponseDto<DvdDto>>('/dvd', {
    params,
  });
  console.log('data', data);

  return data;
};

export const useGetDvds = (params?: GetDvdsParams) => {
  return useQuery({
    queryKey: ['dvds', params],
    queryFn: () => getDvds(params),
  });
};
