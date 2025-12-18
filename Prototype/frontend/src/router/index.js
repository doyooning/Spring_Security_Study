import { createRouter, createWebHistory } from 'vue-router'
import Home from "@/views/Home.vue";
import MyPage from "@/views/MyPage.vue";
import Login from "@/views/Login.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home
    },
    {
      path: "/my",
      component: MyPage
    },
    {
      path: "/login",
      name: "Login",
      component: Login,
    },
  ]
})

export default router
