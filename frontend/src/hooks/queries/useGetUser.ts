import { axiosInstance } from '@/interceptor';
import type { UserDto } from '@/interfaces/responses/UserDto';
import { useQuery } from '@tanstack/react-query';

const getUser = async (): Promise<UserDto> => {
  try {
    const response = await axiosInstance.get<UserDto>('/user');
    return response.data;
  } catch (error) {
    console.error('Error fetching user data', error);
    throw new Error('Użytkownik niezalogowany lub sesja wygasła.');
  }
};

export const useGetUser = () => {
  return useQuery<UserDto, Error>({
    queryKey: ['user', 'me'],
    queryFn: getUser,
    retry: false,
  });
};
