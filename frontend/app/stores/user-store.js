// stores/user-store.js
import { defineStore } from "pinia"
import { useAuthStore } from "./auth-store"

export const useUserStore = defineStore("userStore", () => {
    const config = useRuntimeConfig()
    const api = config.public.apiBase
    const authStore = useAuthStore()

    // ---------------- Headers ----------------
    function getHeaders() {
        if (!authStore.token) throw new Error("Not authenticated")
        return {
            Authorization: `Bearer ${authStore.token}`,
            Accept: "application/json",
            "Content-Type": "application/json"
        }
    }

    // ---------------- USERS ----------------

    // Get all users (page 1)
    async function getAll() {
        return await $fetch(`${api}/users/`, {
            method: "GET",
            headers: getHeaders()
        })
    }

    // Get user by username (profile page)
    async function getByUsername(username) {
        return await $fetch(`${api}/users/${username}`, {
            method: "GET",
            headers: getHeaders()
        })
    }

    // Create user (ADMINISTRADOR)
    async function create(userData) {
        return await $fetch(`${api}/users/`, {
            method: "POST",
            headers: getHeaders(),
            body: userData
        })
    }

    // Update user (ADMINISTRADOR)
    async function update(username, data) {
        return await $fetch(`${api}/users/${username}`, {
            method: "PUT",
            headers: getHeaders(),
            body: data
        })
    }

    // Delete or deactivate user (ADMINISTRADOR)
    async function remove(username) {
        return await $fetch(`${api}/users/${username}`, {
            method: "DELETE",
            headers: getHeaders()
        })
    }

    // Change role (ADMINISTRADOR)
    async function changeRole(username, role) {
        return await $fetch(`${api}/users/${username}/role`, {
            method: "PUT",
            headers: getHeaders(),
            body: { role }
        })
    }

    // Change status (block / activate)
    async function changeStatus(username, blocked) {
        return await $fetch(`${api}/users/${username}/status`, {
            method: "PUT",
            headers: getHeaders(),
            body: { blocked }
        })
    }

    // ---------------- POSTS ----------------

    // Get posts from another user (ADMIN / RESPONSAVEL)
    async function getUserPosts(username) {
        return await $fetch(`${api}/users/${username}/posts`, {
            method: "GET",
            headers: getHeaders()
        })
    }

    // Get my posts
    async function getMyPosts() {
        return await $fetch(`${api}/users/me/posts`, {
            method: "GET",
            headers: getHeaders()
        })
    }

    // Delete another user's post (ADMINISTRADOR only)
    async function deleteUserPost(username, postId) {
        return await $fetch(`${api}/users/${username}/posts/${postId}`, {
            method: "DELETE",
            headers: getHeaders()
        })
    }


    // Delete my post
    async function deleteMyPost(postId) {
        return await $fetch(`${api}/users/me/posts/${postId}`, {
            method: "DELETE",
            headers: getHeaders()
        })
    }

    // ---------------- ME ----------------

    // Update my profile
    async function updateMe(data) {
        return await $fetch(`${api}/users/me`, {
            method: "PUT",
            headers: getHeaders(),
            body: data
        })
    }

    // Change my password
    async function changeMyPassword(oldPassword, newPassword) {
        return await $fetch(`${api}/users/me/password`, {
            method: "PUT",
            headers: getHeaders(),
            body: { oldPassword, newPassword }
        })
    }

    // Get my profile
    async function getMe() {
        return await $fetch(`${api}/users/me`, {
            method: "GET",
            headers: getHeaders()
        })
    }


    // ---------------- HISTORY ----------------

    // Get user history (ADMINISTRADOR)
    async function getUserHistory(username) {
        return await $fetch(`${api}/history/${username}`, {
            method: "GET",
            headers: getHeaders()
        })
    }

    // Get my history
    async function getMyHistory() {
        return await $fetch(`${api}/history/me`, {
            method: "GET",
            headers: getHeaders()
        })
    }

    async function getMyEmails() {

        const response = await $fetch(`${api}/users/me/emails`, {
            method: 'GET',
            headers: getHeaders()
        })

        return response
    }

    // ---------------- EXPORT ----------------
    return {
        getAll,
        getMe,
        getByUsername,
        create,
        update,
        remove,
        changeRole,
        changeStatus,
        getUserPosts,
        getMyPosts,
        deleteMyPost,
        deleteUserPost,
        updateMe,
        changeMyPassword,
        getUserHistory,
        getMyHistory,
        getMyEmails
    }
})
