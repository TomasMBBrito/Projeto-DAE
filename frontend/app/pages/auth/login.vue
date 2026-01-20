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

    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '~/stores/auth-store';
import { useRouter } from 'vue-router'

const router = useRouter()
const config = useRuntimeConfig()
const api = config.public.apiBase

const loginFormData = reactive({
  username: "",
  password: "",
})

const loginError = ref('')


const authStore = useAuthStore();
const { token, user } = storeToRefs(authStore)

const messages = ref([])

function reset() {
  token.value = null
  messages.value = []
  loginFormData.username = ""
  loginFormData.password = ""
}

async function login() {
  loginError.value = ''

  try {
    const response = await $fetch.raw(`${api}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json'
      },
      body: loginFormData
    })

    // SUCCESS
    if (response.status === 200) {
      const authHeader = response.headers.get('authorization')

      if (!authHeader) {
        loginError.value = 'Login failed. Please try again.'
        return
      }

      token.value = authHeader.replace('Bearer ', '')
      getUserInfo()
      // Tentativa de push para apanhar possiveis erros de roteamento
      try {
        await router.push('/publication/searchPublications')
      } catch (navError) {
        console.error('Navigation error:', navError)
        loginError.value = 'An application error occurred. Please contact support or try refreshing the page.'
      }
      return
    }

  } catch (error) {
    // HTTP ERRORS LAND HERE
    const status = error?.response?.status

    if (status === 401 || status === 404) {
      loginError.value = 'Invalid username or password'
    } else {
      loginError.value = 'Unable to connect to the server'
    }
  }
}





async function getUserInfo() {
  try {
    await $fetch(`${api}/auth/user`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
        Authorization: `Bearer ${token.value}`
      },
      onResponse({ request, response, options }) {
        if (response.status == 200) {
          user.value = response._data
        }
      }
    })
  } catch (e) {
    console.error('user info request failed: ', e)
    messages.value.push({ error: e.message })
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

</style>
