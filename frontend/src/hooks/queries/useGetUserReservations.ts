import { useQuery } from "@tanstack/react-query";
import { axiosInstance } from "@/interceptor";
import type { ReservationDto } from "@/interfaces/responses/ReservationDto";
import type { ReservationStatus } from "@/enums/ReservationStatus";

const getUserReservations = async (
  filter?: ReservationStatus
): Promise<ReservationDto[]> => {
  const response = await axiosInstance.get<ReservationDto[]>("/reservations", {
    params: {
      filter,
    },
  });
  return response.data;
};

export const useGetUserReservations = (filter?: ReservationStatus) => {
  return useQuery({
    queryKey: ["userReservations", filter],
    queryFn: () => getUserReservations(filter),
  });
};
