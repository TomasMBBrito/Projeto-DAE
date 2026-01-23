<template>
  <div class="reset-password-container">
    <div class="reset-password-card">
      <h1 class="title">Redefinir Password</h1>
      <p class="subtitle">Insira a sua nova password</p>

      <div class="form-group">
        <label>Nova Password</label>
        <input 
          v-model="formData.password" 
          type="password" 
          class="input"
          placeholder="Nova password"
          :disabled="loading"
        >
      </div>

      <div class="form-group">
        <label>Confirmar Password</label>
        <input 
          v-model="formData.confirmPassword" 
          type="password" 
          class="input"
          placeholder="Confirme a password"
          :disabled="loading"
          @keyup.enter="resetPassword"
        >
      </div>

      <div class="buttons">
        <button 
          class="btn primary" 
          @click="resetPassword"
          :disabled="loading"
        >
          {{ loading ? 'A processar...' : 'Redefinir Password' }}
        </button>
        <NuxtLink to="/auth/login" class="btn secondary">
          Cancelar
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

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const formData = reactive({
  password: '',
  confirmPassword: ''
})

const loading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

// Obter o token da store ou da query string (fallback)
const token = computed(() => authStore.resetToken || route.query.token || '')

onMounted(() => {
  // Verificar se há token
  if (!token.value) {
    errorMessage.value = 'Token inválido ou expirado. Por favor, solicite um novo reset de password.'
  }
})

// Limpar o resetToken quando sair da página
onUnmounted(() => {
  authStore.resetToken = null
})

async function resetPassword() {
  errorMessage.value = ''
  successMessage.value = ''

  // Validações
  if (!formData.password || !formData.confirmPassword) {
    errorMessage.value = 'Por favor preencha todos os campos'
    return
  }

  if (formData.password !== formData.confirmPassword) {
    errorMessage.value = 'As passwords não coincidem'
    return
  }

  if (formData.password.length < 6) {
    errorMessage.value = 'A password deve ter pelo menos 6 caracteres'
    return
  }

  if (!token.value) {
    errorMessage.value = 'Token inválido ou expirado'
    return
  }

  loading.value = true

  try {
    const result = await authStore.completePasswordReset(token.value, formData.password)
    successMessage.value = result.message + ' A redirecionar...'
    
    // Limpar o resetToken
    authStore.resetToken = null
    
    // Redirecionar para login após 2 segundos
    setTimeout(() => {
      router.push('/auth/login')
    }, 2000)

  } catch (error) {
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.reset-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.reset-password-card {
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