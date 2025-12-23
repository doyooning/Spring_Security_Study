<script setup>
import { onMounted, ref } from "vue";

const apiBase = "http://localhost:8080";
const pending = ref(null);
// Token for authorizing signup API calls.
const signupToken = ref("");
const phoneNumber = ref("");
const verificationCode = ref("");
const memberType = ref("GENERAL");
const mbti = ref("");
const job = ref("");
const agreeToTerms = ref(false);
const message = ref("");
const isVerified = ref(false);

const loadPending = async () => {
  if (!signupToken.value) {
    message.value = "토큰이 없습니다. 다시 소셜 로그인을 진행해주세요.";
    return;
  }

  const response = await fetch(`${apiBase}/api/signup/social/pending`, {
    headers: { Authorization: `Bearer ${signupToken.value}` },
    credentials: "include",
  });

  if (!response.ok) {
    message.value = "추가정보 입력을 진행할 수 없습니다.";
    return;
  }

  pending.value = await response.json();
};

const sendCode = async () => {
  if (!signupToken.value) {
    message.value = "토큰이 없습니다. 다시 소셜 로그인을 진행해주세요.";
    return;
  }

  const response = await fetch(`${apiBase}/api/signup/social/phone/send`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${signupToken.value}`,
    },
    credentials: "include",
    body: JSON.stringify({ phoneNumber: phoneNumber.value }),
  });

  if (!response.ok) {
    message.value = "인증번호 전송 실패";
    return;
  }

  const data = await response.json();
  message.value = `인증번호 전송 완료 (개발용 코드: ${data.code})`;
};

const verifyCode = async () => {
  if (!signupToken.value) {
    message.value = "토큰이 없습니다. 다시 소셜 로그인을 진행해주세요.";
    return;
  }

  const response = await fetch(`${apiBase}/api/signup/social/phone/verify`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${signupToken.value}`,
    },
    credentials: "include",
    body: JSON.stringify({
      phoneNumber: phoneNumber.value,
      code: verificationCode.value,
    }),
  });

  if (!response.ok) {
    message.value = "인증번호 확인 실패";
    isVerified.value = false;
    return;
  }

  isVerified.value = true;
  message.value = "전화번호 인증 완료";
};

const submitSignup = async () => {
  if (!signupToken.value) {
    message.value = "토큰이 없습니다. 다시 소셜 로그인을 진행해주세요.";
    return;
  }

  const response = await fetch(`${apiBase}/api/signup/social/complete`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${signupToken.value}`,
    },
    credentials: "include",
    body: JSON.stringify({
      memberType: memberType.value,
      phoneNumber: phoneNumber.value,
      mbti: mbti.value,
      job: job.value,
      agreeToTerms: agreeToTerms.value,
    }),
  });

  if (!response.ok) {
    const errorText = await response.text();
    message.value = errorText || "회원가입 실패";
    return;
  }

  message.value = "회원가입 완료";
  window.location.href = "/my";
};

// Extract signup token from URL query parameter.
const initializeToken = () => {
  const params = new URLSearchParams(window.location.search);
  const token = params.get("token");
  if (token) {
    signupToken.value = token;
    window.history.replaceState({}, "", "/signup");
  }
};

onMounted(() => {
  initializeToken();
  loadPending();
});
</script>

<template>
  <h1>추가 정보 입력</h1>
  <div v-if="pending">
    <p>이름: {{ pending.name }}</p>
    <p>이메일: {{ pending.email }}</p>
  </div>

  <div>
    <h2>전화번호 인증</h2>
    <input v-model="phoneNumber" placeholder="전화번호" />
    <button @click="sendCode">인증번호 받기</button>
    <input v-model="verificationCode" placeholder="인증번호" />
    <button @click="verifyCode">인증하기</button>
    <p v-if="isVerified">인증 완료</p>
  </div>

  <div>
    <h2>회원 종류</h2>
    <select v-model="memberType">
      <option value="GENERAL">일반 회원</option>
    </select>
  </div>

  <div>
    <h2>추가 정보</h2>
    <input v-model="mbti" placeholder="MBTI (선택)" />
    <input v-model="job" placeholder="직업 (선택)" />
  </div>

  <div>
    <label>
      <input type="checkbox" v-model="agreeToTerms" />
      약관 동의
    </label>
  </div>

  <button @click="submitSignup">회원가입 완료</button>
  <p>{{ message }}</p>
</template>

<style scoped>
</style>
