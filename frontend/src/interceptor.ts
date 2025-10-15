import axios from "axios";

export const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_BACKEND_URL,
  withCredentials: true,
});

export const createAxiosInterceptor = () => {
  // Response interceptor for token refresh
  axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalRequest = error.config;

      // Check if error is due to expired token (403) and we haven't retried yet
      if (error.response?.status === 403 && !originalRequest._retry) {
        originalRequest._retry = true;

        try {
          // Make request to refresh token endpoint
          await axios.post(
            `${import.meta.env.VITE_BACKEND_URL}/auth/refresh-tokens`,
            {},
            { withCredentials: true } // refreshToken is included via cookies
          );

          // Retry the original request (now with updated cookies)
          return axiosInstance(originalRequest);
        } catch (refreshError) {
          // If refresh fails, redirect to login
          window.location.href = "/";
          return Promise.reject(refreshError);
        }
      }

      return Promise.reject(error);
    }
  );
};

createAxiosInterceptor();
