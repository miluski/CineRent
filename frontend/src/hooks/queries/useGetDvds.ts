import { useQuery } from "@tanstack/react-query";
import type { DvdDto } from "@/interfaces/responses/DvdDto";
import { axiosInstance } from "@/interceptor";

/**
 * Parameters for filtering the list of DVDs.
 */
interface GetDvdsParams {
  /** Search phrase to match against DVD title and description. */
  "search-phrase"?: string;
  /** List of genre names to filter DVDs by. */
  "genres-names"?: string[];
  /** List of genre identifiers to filter DVDs by. */
  "genres-ids"?: number[];
}

const getDvds = async (params?: GetDvdsParams): Promise<DvdDto[]> => {
  const { data } = await axiosInstance.get<DvdDto[]>("/dvd", {
    params,
  });
  console.log("data", data);

  return data;
};

/**
 * React Query hook to get all DVDs with optional filtering.
 *
 * @param params - Optional filtering parameters for the DVD list.
 * @returns The result of the query, providing the DVD list, loading state, and error information.
 */
export const useGetDvds = (params?: GetDvdsParams) => {
  return useQuery({
    // The query key is an array that uniquely identifies this query.
    // It includes the parameters to ensure that queries with different filters are cached separately.
    queryKey: ["dvds", params],
    // The query function is the async function that fetches the data.
    queryFn: () => getDvds(params),
    // You can add other options here, like `staleTime`, `enabled`, etc.
  });
};
