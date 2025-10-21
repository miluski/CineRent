import { axiosInstance } from '@/interceptor';
import type { VerificationRequestDto } from '@/interfaces/requests/VerificationRequestDto';
import { useMutation, useQueryClient } from '@tanstack/react-query';

export const useVerifyEmail = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: VerificationRequestDto) => {
      const response = await axiosInstance.post('/verification/verify', data);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user', 'me'] });
    },
  });
};
