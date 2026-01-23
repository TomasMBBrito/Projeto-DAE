// stores/auth-store.js
import { defineStore } from "pinia";

export const useAuthStore = defineStore("authStore", () => {
    const token = ref(null)
    const user = ref(null)
    const resetToken = ref(null)
    const config = useRuntimeConfig()
    const api = config.public.apiBase
    
    const isAuthenticated = computed(() => !!token.value)

    async function login(username, password) {
        try {
            const response = await $fetch.raw(`${api}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Accept: 'application/json'
                },
                body: { username, password }
            })

            if (response.status === 200) {
                const authHeader = response.headers.get('authorization')
                
                if (!authHeader) {
                    throw new Error('Login failed. No authorization header.')
                }

                token.value = authHeader.replace('Bearer ', '')
                
                // Save to localStorage
                if (process.client) {
                    localStorage.setItem('auth_token', token.value)
                }
                
                // Get user info
                await getUserInfo()
                
                return { success: true }
            }
        } catch (error) {
            const status = error?.response?.status
            
            if (status === 401 || status === 404) {
                throw new Error('Invalid username or password')
            } else {
                throw new Error('Unable to connect to the server')
            }
        }
    }
    
    function logout() {
        token.value = null
        user.value = null
        if (process.client) {
            localStorage.removeItem('auth_token')
            localStorage.removeItem('auth_user')
        }
    }
    
    function restoreSession() {
        if (process.client) {
            const savedToken = localStorage.getItem('auth_token')
            const savedUser = localStorage.getItem('auth_user')
            if (savedToken) token.value = savedToken
            if (savedUser) user.value = JSON.parse(savedUser)
        }
    }

    async function getUserInfo() {
        try {
            const response = await $fetch(`${api}/auth/user`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    Accept: 'application/json',
                    Authorization: `Bearer ${token.value}`
                }
            })
            
            user.value = response
            
            // Save to localStorage
            if (process.client) {
                localStorage.setItem('auth_user', JSON.stringify(user.value))
            }
            
            return user.value
        } catch (error) {
            console.error('Failed to get user info:', error)
            throw error
        }
    }

    async function requestPasswordReset(email) {
        try {
            const response = await $fetch(`${api}/auth/reset-password`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Accept: 'application/json'
                },
                body: { email }
            })

            const resetToken = response?.['token : '] || response?.token || null
            
            return { 
                success: true, 
                message: 'Se o email existir, receberá instruções de recuperação.' ,
                token: resetToken
            }
        } catch (error) {
            console.error('Password reset request failed:', error)
            throw new Error('Erro ao processar pedido. Tente novamente mais tarde.')
        }
    }

    async function completePasswordReset(resetToken, newPassword) {
        try {
            const response = await $fetch(`${api}/auth/reset-password/complete`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Accept: 'application/json',
                    Authorization: `Bearer ${resetToken}`
                },
                body: { password: newPassword }
            })
            
            return { 
                success: true, 
                message: 'Password redefinida com sucesso!' 
            }
        } catch (error) {
            const status = error?.response?.status
            
            if (status === 401) {
                throw new Error('Token inválido ou expirado. Por favor, solicite um novo reset.')
            } else if (status === 404) {
                throw new Error('Utilizador não encontrado')
            } else {
                throw new Error('Erro ao redefinir password. Tente novamente.')
            }
        }
    }
    
    // Auto restore on creation
    restoreSession()
    
    return { token,resetToken, user, isAuthenticated, logout, restoreSession, requestPasswordReset, completePasswordReset, getUserInfo ,login}
})