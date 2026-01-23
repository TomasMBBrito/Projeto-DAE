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

    // ---------------- EXISTING ----------------
    async function getAll() {
        return await $fetch(`${api}/tags/`, {
            method: 'GET',
            headers: getHeaders()
        })
    }

    async function getSubscribers(tagId) {
        return await $fetch(`${api}/tags/${tagId}/subscribers`, {
            method: 'GET',
            headers: getHeaders()
        })
    }

    async function create(name) {
        return await $fetch(`${api}/tags/`, {
            method: 'POST',
            headers: getHeaders(),
            body: { name }
        })
    }

    async function update(tagId, name) {
        return await $fetch(`${api}/tags/${tagId}`, {
            method: 'PUT',
            headers: getHeaders(),
            body: { name }
        })
    }

    async function remove(tagId) {
        return await $fetch(`${api}/tags/${tagId}`, {
            method: 'DELETE',
            headers: getHeaders()
        })
    }

    // Update tag visibility status
    async function updateStatus(tagId, visible) {
        return await $fetch(`${api}/tags/${tagId}/status`, {
            method: 'PUT',
            headers: getHeaders(),
            body: { visible }
        })
    }

    // ---------------- SUBSCRIPTIONS ----------------
    // Get my subscribed tags
    async function getSubscribed() {
        return await $fetch(`${api}/me/tags/`, {
            method: 'GET',
            headers: getHeaders()
        })
    }

    // Subscribe to a tag
    async function subscribe(tagId) {
        return await $fetch(`${api}/me/tags/`, {
            method: 'POST',
            headers: getHeaders(),
            body: { id: tagId }
        })
    }

    // Unsubscribe from a tag
    async function unsubscribe(tagId) {
        return await $fetch(`${api}/me/tags/${tagId}`, {
            method: 'DELETE',
            headers: getHeaders()
        })
    }

    return {
        getAll,
        getSubscribers,
        create,
        update,
        updateStatus,
        remove,
        getSubscribed,
        subscribe,
        unsubscribe
    }
})