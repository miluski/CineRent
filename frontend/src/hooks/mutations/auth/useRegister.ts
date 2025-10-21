import { axiosInstance } from '@/interceptor';
import type { RegisterRequestDto } from '@/interfaces/requests/RegisterRequestDto';
import { useMutation } from '@tanstack/react-query';

export const useRegister = () => {
  return useMutation({
    mutationFn: async (data: RegisterRequestDto) => {
      const response = await axiosInstance.post('/auth/register', data);
      return response.data;
    },
  });
};
