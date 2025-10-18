import { useQuery } from "@tanstack/react-query";
import { axiosInstance } from "@/interceptor";
import type { TransactionDto } from "@/interfaces/responses/TransactionDto";

const getTransactionsHistory = async (): Promise<TransactionDto[]> => {
  const response = await axiosInstance.get<TransactionDto[]>("/transactions");
  return response.data;
};

export const useGetTransactionsHistory = () => {
  return useQuery({
    queryKey: ["transactionsHistory"],
    queryFn: getTransactionsHistory,
  });
};
