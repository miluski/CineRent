import { useQuery } from "@tanstack/react-query";
import { axiosInstance } from "@/interceptor";

interface User {
  nickname: string;
  age: number;
  preferredGenres: string[];
}

const getUser = async (): Promise<User> => {
  try {
    const response = await axiosInstance.get<User>("/user");
    return response.data;
  } catch (error) {
    console.error("Error fetching user data", error);
    throw new Error("Użytkownik niezalogowany lub sesja wygasła.");
  }
};

export const useGetUser = () => {
  return useQuery<User, Error>({
    queryKey: ["user", "me"],
    queryFn: getUser,
    retry: false,
  });
};
