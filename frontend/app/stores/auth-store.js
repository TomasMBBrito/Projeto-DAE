// stores/auth-store.js
import { defineStore } from "pinia";

export const useAuthStore = defineStore("authStore", () => {
    const token = ref(null)
    const user = ref(null)
    
    const isAuthenticated = computed(() => !!token.value)
    
    function logout() {
        token.value = null
        user.value = null
        if (process.client) {
            localStorage.removeItem('auth_token')
            localStorage.removeItem('auth_user')
        }
    }
    
    function setUser(userData) {
        user.value = userData
        if (process.client && userData) {
            localStorage.setItem('auth_user', JSON.stringify(userData))
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
    
    // Auto restore on creation
    restoreSession()
    
    return { token, user, isAuthenticated, logout, restoreSession, setUser }
})