import axios from "axios";

export const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_BACKEND_URL,
  withCredentials: true,
});

export const createAxiosInterceptor = () => {
  axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalRequest = error.config;

      if (
        location.pathname !== "/" &&
        error.response?.status === 401 &&
        !originalRequest._retry
      ) {
        originalRequest._retry = true;

        try {
          await axios.post(
            `${import.meta.env.VITE_BACKEND_URL}/auth/refresh-tokens`,
            {},
            { withCredentials: true }
          );

          return axiosInstance(originalRequest);
        } catch (refreshError) {
          window.location.href = "/";
          return Promise.reject(refreshError);
        }
      }

      return Promise.reject(error);
    }
  );
};

createAxiosInterceptor();
