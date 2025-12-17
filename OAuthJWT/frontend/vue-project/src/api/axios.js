import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8080",
    withCredentials: true, // 쿠키 포함
});

// 요청 인터셉터
api.interceptors.request.use((config) => {
    const accessToken = sessionStorage.getItem("accessToken");
    if (accessToken) {
        config.headers.Authorization = "Bearer " + accessToken;
    }
    return config;
});

// 응답 인터셉터 (Access Token 만료 처리)
api.interceptors.response.use(
    (res) => res,
    async (err) => {
        if (
            err.response?.status === 401 &&
            err.response.data === "ACCESS_TOKEN_EXPIRED"
        ) {
            // Refresh Token으로 재발급
            const res = await axios.post(
                "http://localhost:8080/auth/reissue",
                {},
                { withCredentials: true }
            );

            const newAccessToken = res.data.accessToken;

            sessionStorage.setItem("accessToken", newAccessToken);
            err.config.headers.Authorization =
                "Bearer " + newAccessToken;

            return axios(err.config);
        }

        return Promise.reject(err);
    }
);

export default api;