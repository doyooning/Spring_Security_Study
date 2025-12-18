<script setup>
const onNaverLogin = () => {
  window.location.href =
      "http://localhost:8080/oauth2/authorization/naver";
};

const onGoogleLogin = () => {
  window.location.href =
      "http://localhost:8080/oauth2/authorization/google";
};
</script>

<template>
  <h1>Login</h1>
  <button @click="onNaverLogin">Naver Login</button>
  <button @click="onGoogleLogin">Google Login</button>
</template>

<style scoped>

</style>