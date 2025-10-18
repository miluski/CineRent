import { useQuery } from "@tanstack/react-query";
import { axiosInstance } from "@/interceptor";
import type { ReservationDto } from "@/interfaces/responses/ReservationDto";

const getAllReturnRequests = async (): Promise<ReservationDto[]> => {
  const response = await axiosInstance.get<ReservationDto[]>(
    "/rentals/return-requests"
  );
  return response.data;
};

export const useGetAllReturnRequests = () => {
  return useQuery({
    queryKey: ["allReturnRequests"],
    queryFn: getAllReturnRequests,
  });
};
