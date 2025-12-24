<script setup>
const logout = async () => {
  const access = localStorage.getItem('access') || sessionStorage.getItem('access');
  const headers = {};
  if (access) {
    headers.access = access;
  }

  try {
    await fetch('http://localhost:8080/logout', {
      method: 'POST',
      credentials: 'include',
      headers
    });
  } catch (error) {
    console.error('logout failed', error);
  } finally {
    localStorage.removeItem('access');
    sessionStorage.removeItem('access');
    window.location.href = '/';
  }
};
</script>

<template>
  <section class="home">
    <h1>Home</h1>
    <button class="logout" type="button" @click="logout">Logout</button>
  </section>
</template>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 16px;
  padding: 32px;
}

.logout {
  border: 1px solid #222;
  background: #fff;
  color: #222;
  padding: 10px 16px;
  cursor: pointer;
  font-size: 14px;
}
</style>
