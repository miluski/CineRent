import { useQuery } from "@tanstack/react-query";
import { axiosInstance } from "@/interceptor";
import type { ReservationDto } from "@/interfaces/responses/ReservationDto";

const getRentals = async (filter?: "HISTORICAL"): Promise<ReservationDto[]> => {
  const response = await axiosInstance.get<ReservationDto[]>("/rentals", {
    params: {
      filter,
    },
  });
  return response.data;
};

export const useGetRentals = (filter?: "HISTORICAL") => {
  return useQuery({
    queryKey: ["rentals", filter],
    queryFn: () => getRentals(filter),
  });
};
