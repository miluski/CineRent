import { axiosInstance } from '@/interceptor';
import type { CreateReminderRequestDto } from '@/interfaces/requests/CreateReminderRequestDto';
import { useMutation } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { toast } from 'sonner';

const createReminder = async (reminderData: CreateReminderRequestDto) => {
  const { data } = await axiosInstance.post('/reminders', reminderData);
  return data;
};

export const useCreateReminder = () => {
  return useMutation({
    mutationFn: createReminder,
    onSuccess: () => {
      toast.success('Powiadomienie zostało ustawione. Otrzymasz email gdy film będzie dostępny.');
    },
    onError: (error: AxiosError) => {
      if (error.response?.status === 403) {
        toast.error('Musisz zweryfikować swój adres email, aby ustawić powiadomienia.');
      } else if (error.response?.status === 404) {
        toast.error('Nie znaleziono filmu.');
      } else {
        toast.error('Wystąpił błąd podczas ustawiania powiadomienia. Spróbuj ponownie.');
      }
    },
  });
};
