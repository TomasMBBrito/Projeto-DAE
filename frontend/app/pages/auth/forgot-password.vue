<template>
  <div class="forgot-password-container">
    <div class="forgot-password-card">
      <h1 class="title">Recuperar Password</h1>
      <p class="subtitle">Insira o seu email para receber instruções de recuperação</p>

      <div class="form-group">
        <label>Email</label>
        <input 
          v-model="email" 
          type="email" 
          class="input"
          placeholder="seu.email@exemplo.com"
          :disabled="loading"
        >
      </div>

      <div class="buttons">
        <button 
          class="btn primary" 
          @click="requestReset"
          :disabled="loading"
        >
          {{ loading ? 'A enviar...' : 'Enviar' }}
        </button>
        <NuxtLink to="/auth/login" class="btn secondary">
          Voltar ao Login
        </NuxtLink>
      </div>

      <div v-if="successMessage" class="success-message">
        {{ successMessage }}
      </div>

      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '~/stores/auth-store'
const authStore = useAuthStore()
const router = useRouter()

const email = ref('')
const loading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

async function requestReset() {
  if (!email.value || !email.value.trim()) {
    errorMessage.value = 'Por favor insira um email válido'
    return
  }

  errorMessage.value = ''
  successMessage.value = ''
  loading.value = true

  try {
    const result = await authStore.requestPasswordReset(email.value)
    if (result.token) {
      authStore.resetToken = result.token
      
      await router.push('/auth/reset-password')
    } else {
      // Se não houver token, mostrar mensagem e redirecionar após 3 segundos
      successMessage.value = result.message
      setTimeout(() => {
        router.push('/auth/login')
      }, 3000)
    }


  } catch (error) {
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.forgot-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.forgot-password-card {
  background: #f9f9f9;
  width: 450px;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 2px 2px 10px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  margin-bottom: 10px;
  font-size: 28px;
}

.subtitle {
  text-align: center;
  color: #666;
  font-size: 14px;
  margin-bottom: 25px;
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

.input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 20px 0;
}

.btn {
  padding: 10px 18px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 15px;
  text-align: center;
  text-decoration: none;
  display: inline-block;
}

.btn.primary {
  background: #0077cc;
  color: white;
}

.btn.primary:hover:not(:disabled) {
  background: #005fa3;
}

.btn.primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn.secondary {
  background: #ddd;
  color: #333;
}

.btn.secondary:hover {
  background: #ccc;
}

.success-message {
  margin-top: 15px;
  padding: 10px 12px;
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
  border-radius: 6px;
  font-size: 14px;
  text-align: center;
}

.error-message {
  margin-top: 15px;
  padding: 10px 12px;
  background: #fdecea;
  color: #b42318;
  border: 1px solid #f5c2c7;
  border-radius: 6px;
  font-size: 14px;
  text-align: center;
}
</style>