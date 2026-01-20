// stores/tag-store.js
import { defineStore } from "pinia";
import { useAuthStore } from "./auth-store";

export const useTagStore = defineStore("tagStore", () => {
    const config = useRuntimeConfig()
    const api = config.public.apiBase
    const authStore = useAuthStore()

    function getHeaders() {
        if (!authStore.token) throw new Error("Not authenticated")
        return {
            'Authorization': `Bearer ${authStore.token}`,
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    }

    // Get all tags
    async function getAll() {
        return await $fetch(`${api}/tags/`, {
            method: 'GET',
            headers: getHeaders()
        })
    }

    // Get subscribers for a tag
    async function getSubscribers(tagId) {
        return await $fetch(`${api}/tags/${tagId}/subscribers`, {
            method: 'GET',
            headers: getHeaders()
        })
    }

    // Create a new tag
    async function create(name) {
        return await $fetch(`${api}/tags/`, {
            method: 'POST',
            headers: getHeaders(),
            body: { name }
        })
    }

    // Update a tag
    async function update(tagId, name) {
        return await $fetch(`${api}/tags/${tagId}`, {
            method: 'PUT',
            headers: getHeaders(),
            body: { name }
        })
    }

    // Delete a tag
    async function remove(tagId) {
        return await $fetch(`${api}/tags/${tagId}`, {
            method: 'DELETE',
            headers: getHeaders()
        })
    }

    return {
        getAll,
        getSubscribers,
        create,
        update,
        remove
    }
})
