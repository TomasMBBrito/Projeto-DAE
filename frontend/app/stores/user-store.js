// stores/user-store.js
import { defineStore } from "pinia";
import { useAuthStore } from "./auth-store";

export const useUserStore = defineStore("userStore", () => {
    const config = useRuntimeConfig()
    const api = config.public.apiBase
    const authStore = useAuthStore()

    async function getAll() {
        // Debug: verifica se tem token
        console.log('Token:', authStore.token)
        
        if (!authStore.token) {
            throw new Error('Not authenticated')
        }
        
        const response = await $fetch(`${api}/users/`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${authStore.token}`
            }
        })

        return response
    }

    async function getById(id) {
        if (!authStore.token) {
            throw new Error('Not authenticated')
        }
        
        const response = await $fetch(`${api}/users/${id}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${authStore.token}`,
                'Accept': 'application/json'
            }
        })

        return response
    }

    async function getPublicationsByUsername(username) {
        if (!authStore.token) {
            throw new Error('Not authenticated')
        }
        
        const response = await $fetch(`${api}/users/${username}/posts`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${authStore.token}`,
                'Accept': 'application/json'
            }
        })

        return response
    }

    return { getAll, getById, getPublicationsByUsername }
})