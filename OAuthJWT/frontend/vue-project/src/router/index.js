import { createRouter, createWebHistory } from "vue-router";
import Login from "@/views/Login.vue";
import LoginSuccess from "@/views/LoginSuccess.vue";
import MyPage from "@/views/MyPage.vue";

const routes = [
    {
        path: "/",
        redirect: "/login",
    },
    {
        path: "/login",
        name: "Login",
        component: Login,
    },
    {
        path: "/login/success",
        name: "LoginSuccess",
        component: LoginSuccess,
    },
    {
        path: "/mypage",
        component: MyPage
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;
