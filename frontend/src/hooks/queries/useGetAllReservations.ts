import { useQuery } from "@tanstack/react-query";
import { axiosInstance } from "@/interceptor";
import type { ReservationDto } from "@/interfaces/responses/ReservationDto";
import type { ReservationStatus } from "@/enums/ReservationStatus";

const getAllReservations = async (
  filter?: ReservationStatus
): Promise<ReservationDto[]> => {
  const response = await axiosInstance.get<ReservationDto[]>(
    "/reservations/all",
    {
      params: {
        filter,
      },
    }
  );
  return response.data;
};

export const useGetAllReservations = (filter?: ReservationStatus) => {
  return useQuery({
    queryKey: ["allReservations", filter],
    queryFn: () => getAllReservations(filter),
  });
};
