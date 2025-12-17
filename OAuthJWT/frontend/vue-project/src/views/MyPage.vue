<script setup>
import { onMounted } from "vue";
import axios from "axios";

onMounted(() => {
  axios.get("http://localhost:8080/my", {
    withCredentials: true,
  })
      .then((res) => {
        alert(JSON.stringify(res.data));
      })
      .catch((err) => {
        console.error(err);
        alert("인증 실패");
      });
});
</script>

<template>
  <h1>My Page</h1>
  <p>로그인 후 접근한 페이지</p>
</template>

<style scoped>

</style>