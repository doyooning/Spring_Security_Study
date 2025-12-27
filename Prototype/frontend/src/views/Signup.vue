<script setup>
import { computed, onMounted, reactive, ref } from "vue";

const apiBase = "http://localhost:8080";
const pending = ref(null);
// Token for authorizing signup API calls.
const signupToken = ref("");
// Token from invitation link for invited sellers.
const inviteToken = ref("");
// Invitation error message for expired/invalid tokens.
const inviteError = ref("");
// Signup form state grouped into a single reactive object.
const form = reactive({
  phoneNumber: "",
  verificationCode: "",
  memberType: "GENERAL",
  mbti: "",
  job: "",
  businessNumber: "",
  companyName: "",
  description: "",
  planFileBase64: "",
  planFileName: "",
  agreeToTerms: false,
  message: "",
  isVerified: false,
});
// Flag for invite-based signup flow.
const isInviteSignup = computed(() => !!inviteToken.value);

const loadPending = async () => {
  if (!signupToken.value) {
    form.message = "Login required to continue signup.";
    return;
  }

  const response = await fetch(`${apiBase}/api/signup/social/pending`, {
    headers: { Authorization: `Bearer ${signupToken.value}` },
    credentials: "include",
  });

  if (!response.ok) {
    form.message = "Unable to load pending signup data.";
    return;
  }

  pending.value = await response.json();
};

const sendCode = async () => {
  if (!signupToken.value) {
    form.message = "Login required to continue signup.";
    return;
  }

  const response = await fetch(`${apiBase}/api/signup/social/phone/send`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${signupToken.value}`,
    },
    credentials: "include",
    body: JSON.stringify({ phoneNumber: form.phoneNumber }),
  });

  if (!response.ok) {
    form.message = "Failed to send verification code.";
    return;
  }

  const data = await response.json();
  form.message = `Verification code sent (dev code: ${data.code})`;
};

const verifyCode = async () => {
  if (!signupToken.value) {
    form.message = "Login required to continue signup.";
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
      phoneNumber: form.phoneNumber,
      code: form.verificationCode,
    }),
  });

  if (!response.ok) {
    form.message = "Verification failed.";
    form.isVerified = false;
    return;
  }

  form.isVerified = true;
  form.message = "Phone verification complete.";
};

const submitSignup = async () => {
  if (!signupToken.value) {
    form.message = "Login required to continue signup.";
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
      memberType: form.memberType,
      phoneNumber: form.phoneNumber,
      mbti: form.mbti,
      job: form.job,
      businessNumber: form.businessNumber,
      companyName: form.companyName,
      description: form.description,
      planFileBase64: form.planFileBase64,
      inviteToken: inviteToken.value,
      agreeToTerms: form.agreeToTerms,
    }),
  });

  if (!response.ok) {
    const errorText = await response.text();
    form.message = errorText || "Signup failed.";
    return;
  }

  const successText = await response.text();
  if (form.memberType === "SELLER") {
    form.message =
      successText ||
      "Seller signup submitted. Await admin review.";
    return;
  }

  form.message = successText || "Signup completed.";
  window.location.href = "/my";
};

// Capture seller plan file and store as base64 for API payload.
const handlePlanFileChange = (event) => {
  const file = event.target.files?.[0];
  if (!file) {
    form.planFileBase64 = "";
    form.planFileName = "";
    return;
  }

  form.planFileName = file.name;
  const reader = new FileReader();
  reader.onload = () => {
    form.planFileBase64 = reader.result || "";
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
    form.memberType = "SELLER";
    return;
  }

  const storedToken = sessionStorage.getItem("inviteToken");
  if (storedToken) {
    inviteToken.value = storedToken;
    form.memberType = "SELLER";
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
      <input v-model="form.phoneNumber" placeholder="전화번호" />
      <button @click="sendCode">인증번호 받기</button>
      <input v-model="form.verificationCode" placeholder="인증번호" />
      <button @click="verifyCode">인증하기</button>
      <p v-if="form.isVerified">인증 완료</p>
    </div>

    <div>
      <h2>회원 종류</h2>
      <select v-model="form.memberType" :disabled="isInviteSignup">
        <option value="GENERAL">일반 회원</option>
        <option value="SELLER">판매자</option>
      </select>
      <p v-if="isInviteSignup">초대받은 판매자는 판매자로만 가입할 수 있습니다.</p>
    </div>

    <div v-if="form.memberType === 'GENERAL'">
      <h2>추가 정보</h2>
      <input v-model="form.mbti" placeholder="MBTI (선택)" />
      <input v-model="form.job" placeholder="직업 (선택)" />
    </div>

    <div v-else-if="form.memberType === 'SELLER' && !isInviteSignup">
      <h2>판매자 정보</h2>
      <input v-model="form.businessNumber" placeholder="사업자등록번호" />
      <input v-model="form.companyName" placeholder="사업자명" />
      <textarea v-model="form.description" placeholder="사업 설명 (선택)"></textarea>
      <input type="file" @change="handlePlanFileChange" />
      <p v-if="form.planFileName">선택된 파일: {{ form.planFileName }}</p>
    </div>

    <div>
      <label>
        <input type="checkbox" v-model="form.agreeToTerms" />
        약관 동의
      </label>
    </div>

    <button @click="submitSignup">회원가입 완료</button>
  </div>
  <p>{{ form.message }}</p>
</template>

<style scoped>
</style>
