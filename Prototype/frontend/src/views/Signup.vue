<script setup>
import { computed, onMounted, ref } from "vue";

const apiBase = "http://localhost:8080";
const pending = ref(null);
// Token for authorizing signup API calls.
const signupToken = ref("");
// Token from invitation link for invited sellers.
const inviteToken = ref("");
// Invitation error message for expired/invalid tokens.
const inviteError = ref("");
const phoneNumber = ref("");
const verificationCode = ref("");
const memberType = ref("GENERAL");
const mbti = ref("");
const job = ref("");
// Seller business registration number.
const businessNumber = ref("");
// Seller company name.
const companyName = ref("");
// Optional seller description.
const description = ref("");
// Base64-encoded seller plan file payload.
const planFileBase64 = ref("");
// Display name for selected plan file.
const planFileName = ref("");
const agreeToTerms = ref(false);
const message = ref("");
const isVerified = ref(false);
// Flag for invite-based signup flow.
const isInviteSignup = computed(() => !!inviteToken.value);

const loadPending = async () => {
  if (!signupToken.value) {
    message.value = "Login required to continue signup.";
    return;
  }

  const response = await fetch(`${apiBase}/api/signup/social/pending`, {
    headers: { Authorization: `Bearer ${signupToken.value}` },
    credentials: "include",
  });

  if (!response.ok) {
    message.value = "Unable to load pending signup data.";
    return;
  }

  pending.value = await response.json();
};

const sendCode = async () => {
  if (!signupToken.value) {
    message.value = "Login required to continue signup.";
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
    message.value = "Failed to send verification code.";
    return;
  }

  const data = await response.json();
  message.value = `Verification code sent (dev code: ${data.code})`;
};

const verifyCode = async () => {
  if (!signupToken.value) {
    message.value = "Login required to continue signup.";
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
    message.value = "Verification failed.";
    isVerified.value = false;
    return;
  }

  isVerified.value = true;
  message.value = "Phone verification complete.";
};

const submitSignup = async () => {
  if (!signupToken.value) {
    message.value = "Login required to continue signup.";
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
      businessNumber: businessNumber.value,
      companyName: companyName.value,
      description: description.value,
      planFileBase64: planFileBase64.value,
      inviteToken: inviteToken.value,
      agreeToTerms: agreeToTerms.value,
    }),
  });

  if (!response.ok) {
    const errorText = await response.text();
    message.value = errorText || "Signup failed.";
    return;
  }

  const successText = await response.text();
  if (memberType.value === "SELLER") {
    message.value =
      successText ||
      "Seller signup submitted. Await admin review.";
    return;
  }

  message.value = successText || "Signup completed.";
  window.location.href = "/my";
};

// Capture seller plan file and store as base64 for API payload.
const handlePlanFileChange = (event) => {
  const file = event.target.files?.[0];
  if (!file) {
    planFileBase64.value = "";
    planFileName.value = "";
    return;
  }

  planFileName.value = file.name;
  const reader = new FileReader();
  reader.onload = () => {
    planFileBase64.value = reader.result || "";
  };
  reader.readAsDataURL(file);
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

// Extract invite token from URL or session storage.
const initializeInviteToken = () => {
  const params = new URLSearchParams(window.location.search);
  const token = params.get("invite");
  if (token) {
    inviteToken.value = token;
    sessionStorage.setItem("inviteToken", token);
    window.history.replaceState({}, "", "/signup");
    memberType.value = "SELLER";
    return;
  }

  const storedToken = sessionStorage.getItem("inviteToken");
  if (storedToken) {
    inviteToken.value = storedToken;
    memberType.value = "SELLER";
  }
};

// Validate invitation token to show expired link errors early.
const validateInviteToken = async () => {
  if (!inviteToken.value) {
    return;
  }

  const response = await fetch(
    `${apiBase}/api/invitations/validate?token=${encodeURIComponent(inviteToken.value)}`,
    { credentials: "include" }
  );

  if (!response.ok) {
    const errorText = await response.text();
    inviteError.value = errorText || "Invitation token is invalid.";
  }
};

// Start OAuth login from the signup screen.
const startLogin = (provider) => {
  window.location.href = `${apiBase}/oauth2/authorization/${provider}`;
};

onMounted(() => {
  initializeInviteToken();
  validateInviteToken();
  initializeToken();
  loadPending();
});
</script>

<template>
  <h1>Signup</h1>
  <div v-if="inviteError">
    <p>{{ inviteError }}</p>
  </div>
  <div v-if="!signupToken">
    <p>Login required to continue.</p>
    <button @click="startLogin('naver')">Naver Login</button>
    <button @click="startLogin('google')">Google Login</button>
    <button @click="startLogin('kakao')">Kakao Login</button>
  </div>

  <div v-if="signupToken">
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
      <select v-model="memberType" :disabled="isInviteSignup">
        <option value="GENERAL">일반 회원</option>
        <option value="SELLER">판매자</option>
      </select>
      <p v-if="isInviteSignup">초대받은 판매자는 판매자로만 가입할 수 있습니다.</p>
    </div>

    <div v-if="memberType === 'GENERAL'">
      <h2>추가 정보</h2>
      <input v-model="mbti" placeholder="MBTI (선택)" />
      <input v-model="job" placeholder="직업 (선택)" />
    </div>

    <div v-else-if="memberType === 'SELLER' && !isInviteSignup">
      <h2>판매자 정보</h2>
      <input v-model="businessNumber" placeholder="사업자등록번호" />
      <input v-model="companyName" placeholder="사업자명" />
      <textarea v-model="description" placeholder="사업 설명 (선택)"></textarea>
      <input type="file" @change="handlePlanFileChange" />
      <p v-if="planFileName">선택된 파일: {{ planFileName }}</p>
    </div>

    <div>
      <label>
        <input type="checkbox" v-model="agreeToTerms" />
        약관 동의
      </label>
    </div>

    <button @click="submitSignup">회원가입 완료</button>
  </div>
  <p>{{ message }}</p>
</template>

<style scoped>
</style>
