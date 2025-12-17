<script setup>
import { onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import axios from "axios";

const route = useRoute();
const router = useRouter();

onMounted(() => {

  const accessToken = route.query.accessToken;

  if (!accessToken) {
    alert("로그인 실패");
    return;
  }

  // 1️. Access Token 저장 (메모리 or sessionStorage)
  sessionStorage.setItem("accessToken", accessToken);

  // 2️. axios 기본 헤더 세팅
  axios.defaults.headers.common.Authorization =
      "Bearer " + accessToken;

  // 3️. 메인 페이지로 이동
  router.push("/main");
});
</script>

<template>
  <p>로그인 처리 중...</p>
</template>

<style scoped>

</style>