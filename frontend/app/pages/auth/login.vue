<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="title">Login</h1>

      <div class="form-group">
        <label>Username</label>
        <input v-model="loginFormData.username" type="text" class="input">
      </div>

      <div class="form-group">
        <label>Password</label>
        <input v-model="loginFormData.password" type="password" class="input">
      </div>

      <div class="buttons">
        <button class="btn primary" @click="login">Login</button>
        <button class="btn secondary" @click="reset">Reset</button>
      </div>

      <div v-if="loginError" class="login-error">
        {{ loginError }}
      </div>

      <div class="forgot-password">
        <NuxtLink to="/auth/forgot-password">Esqueceu a password?</NuxtLink>
      </div>

    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '~/stores/auth-store';
import { useRouter } from 'vue-router'

const router = useRouter()
const authStore = useAuthStore()

const loginFormData = reactive({
  username: "",
  password: "",
})

const loginError = ref('')
const loading = ref(false)

const { token} = storeToRefs(authStore)

const messages = ref([])

function reset() {
  token.value = null
  messages.value = []
  loginFormData.username = ""
  loginFormData.password = ""
  loginError.value = ""
}

async function login() {
  if (!loginFormData.username || !loginFormData.password) {
    loginError.value = 'Por favor preencha todos os campos'
    return
  }

  loginError.value = ''
  loading.value = true

 try {
    await authStore.login(loginFormData.username, loginFormData.password)
    
    // Redirect on success
    try {
      await router.push('/publication/searchPublications')
    } catch (navError) {
      console.error('Navigation error:', navError)
      loginError.value = 'An application error occurred. Please contact support or try refreshing the page.'
    }
  } catch (error) {
    loginError.value = error.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.login-card {
  background: #f9f9f9;
  width: 400px;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 2px 2px 10px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  margin-bottom: 25px;
  font-size: 28px;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  font-size: 14px;
  margin-bottom: 5px;
}

.input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 15px;
}

.input:focus {
  outline: none;
  border-color: #0077cc;
  box-shadow: 0 0 4px rgba(0, 119, 204, 0.3);
}

.buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin: 20px 0;
}

.btn {
  padding: 10px 18px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 15px;
}

.btn.primary {
  background: #0077cc;
  color: white;
}

.btn.primary:hover {
  background: #005fa3;
}

.btn.secondary {
  background: #ddd;
  color: #333;
}

.btn.secondary:hover {
  background: #ccc;
}

/* Boxes */
.token-box,
.user-box,
.messages-box {
  margin-top: 20px;
  background: white;
  padding: 15px;
  border-radius: 6px;
  border: 1px solid #ddd;
}

pre {
  background: #f4f4f4;
  padding: 10px;
  border-radius: 6px;
  max-height: 200px;
  overflow: auto;
}

.login-error {
  margin-top: 12px;
  padding: 8px 12px;
  background: #fdecea;
  color: #b42318;
  border: 1px solid #f5c2c7;
  border-radius: 6px;
  font-size: 14px;
  text-align: center;
}

.forgot-password {
  margin-top: 20px;
  text-align: center;
}

.forgot-password a {
  color: #0077cc;
  text-decoration: none;
  font-size: 14px;
}

.forgot-password a:hover {
  text-decoration: underline;
}

</style>
