import { axiosInstance } from '@/interceptor';
import type { ResendVerificationRequestDto } from '@/interfaces/requests/ResendVerificationRequestDto';
import { useMutation } from '@tanstack/react-query';

export const useResendVerification = () => {
  return useMutation({
    mutationFn: async (data: ResendVerificationRequestDto) => {
      const response = await axiosInstance.post('/verification/resend', data);
      return response.data;
    },
  });
};
