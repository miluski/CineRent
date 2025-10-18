import { useQuery } from "@tanstack/react-query";
import { axiosInstance } from "@/interceptor";
import type { ReservationDto } from "@/interfaces/responses/ReservationDto";

const getReturnRequests = async (): Promise<ReservationDto[]> => {
  const response = await axiosInstance.get<ReservationDto[]>(
    "/rentals/return-requests"
  );
  return response.data;
};

export const useGetReturnRequests = () => {
  return useQuery({
    queryKey: ["returnRequests"],
    queryFn: getReturnRequests,
  });
};
