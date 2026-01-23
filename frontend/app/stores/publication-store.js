// stores/publication-store.js
import { defineStore } from "pinia";
import { useAuthStore } from "./auth-store";

export const usePublicationStore = defineStore("publicationStore", () => {
    const config = useRuntimeConfig()
    const api = config.public.apiBase

    async function create(formData) {
        const authStore = useAuthStore()

        if (!authStore.token) {
            throw new Error('Not authenticated')
        }

        const response = await $fetch(`${api}/posts`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${authStore.token}`,
                // ⚠️ NÃO definir Content-Type aqui
                // o browser trata automaticamente o multipart/form-data
                'Accept': 'application/json'
            },
            body: formData
        })

        return response
    }

    async function getAll(sortBy = '') {
        const authStore = useAuthStore()
        
        // Debug: verifica se tem token
        console.log('Token:', authStore.token)
        
        if (!authStore.token) {
            throw new Error('Not authenticated')
        }
        
        const url = sortBy ? `${api}/posts/?sort=${sortBy}` : `${api}/posts/`

        const response = await $fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${authStore.token}`,
                'Accept': 'application/json'
            }
        })

        return response
    }

    async function getById(id) {
        const authStore = useAuthStore()
        
        if (!authStore.token) {
            throw new Error('Not authenticated')
        }

        const response = await $fetch(`${api}/posts/${id}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${authStore.token}`,
                'Accept': 'application/json'
            }
        })

        return response
    }

    async function search(searchTerm) {
        const authStore = useAuthStore()
        
        if (!authStore.token) {
            throw new Error('Not authenticated')
        }

        const response = await $fetch(`${api}/posts/search?q=${encodeURIComponent(searchTerm)}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${authStore.token}`,
                'Accept': 'application/json'
            }
        })

        return response
    }

    async function filterByTags(tagIds) {
        const authStore = useAuthStore()

        if (!authStore.token) {
            throw new Error('Not authenticated')
        }

        if (!tagIds || tagIds.length === 0) {
            return []
        }

        const tagIdsString = tagIds.join(',')
        const response = await $fetch(`${api}/posts/filter/tags?tagIds=${tagIdsString}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${authStore.token}`,
                'Accept': 'application/json'
            }
        })

        return response
    }

    async function getComments(publicationId) {
        const authStore = useAuthStore()
        
        if (!authStore.token) {
            throw new Error('Not authenticated')
        }

        const response = await $fetch(`${api}/posts/${publicationId}/comments`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${authStore.token}`,
                'Accept': 'application/json'
            }
        })

        return response
    }

    async function addComment(publicationId, content) {
        const authStore = useAuthStore()
        
        if (!authStore.token) {
            throw new Error('Not authenticated')
        }

        const response = await $fetch(`${api}/posts/${publicationId}/comments`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${authStore.token}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: { content }
        })

        return response
    }

    async function addRating(publicationId, rating) {
        const authStore = useAuthStore()
        
        if (!authStore.token) {
            throw new Error('Not authenticated')
        }

        const response = await $fetch(`${api}/posts/${publicationId}/ratings`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${authStore.token}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: { rating }
        })

        return response
    }

    async function getHistory(publicationId) {
    const authStore = useAuthStore()

    if (!authStore.token) {
        throw new Error('Not authenticated')
    }

    if (!publicationId) {
        throw new Error('Publication ID is required')
    }

    const response = await $fetch(`${api}/history/posts/${publicationId}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${authStore.token}`,
            'Accept': 'application/json'
        }
    })

    return response
}

    return {
        create,
        getAll,
        getById,
        search,
        filterByTags,
        getComments,
        addComment,
        addRating,
        getHistory
    }
})