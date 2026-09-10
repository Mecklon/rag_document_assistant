import axios from "axios";

const baseURL = "http://localhost:9090/"

const api = axios.create({
  baseURL,
});

api.interceptors.request.use((config) => {
  if (config.skipAuth) return config;
  const token = localStorage.getItem("JwtToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
