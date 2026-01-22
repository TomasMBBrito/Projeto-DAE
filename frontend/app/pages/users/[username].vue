<template>
    <div class="user-profile">
        <div class="loading" v-if="loading">Loading...</div>
        <div class="error" v-else-if="error">{{ error }}</div>

        <template v-else>
            <div class="profile-header">
                <h1>{{ user.username }}</h1>
                <button @click="goBack" class="btn-back">← Back to Users</button>
                <button @click="goToHistory" class="btn-history">View History</button>
            </div>

            <div class="profile-card">
                <div class="profile-field">
                    <span>Email:</span> {{ user.email }}
                </div>
                <div class="profile-field">
                    <span>Role:</span> {{ user.role }}
                </div>
                <div class="profile-field" v-if="user.name">
                    <span>Name:</span> {{ user.name }}
                </div>

                <!-- Admin controls -->
                <div v-if="isAdmin && !isMe" class="admin-controls">
                    <select v-model="role" @change="changeRole" class="select-role">
                        <option>COLABORADOR</option>
                        <option>RESPONSAVEL</option>
                        <option>ADMINISTRADOR</option>
                    </select>

                    <button @click="toggleStatus" class="btn-save">
                        {{ user.blocked ? 'Activate' : 'Block' }}
                    </button>
                </div>
            </div>

            <!-- Posts -->
            <div class="posts-section">
                <h2>Posts</h2>

                <div class="empty" v-if="posts.length === 0">No posts yet</div>

                <div v-else class="posts-list">
                    <div v-for="post in posts" :key="post.id" class="post-card">
                        <div class="post-title" @click="goToPost(post.id)" style="cursor: pointer;">
                            {{ post.title }}
                        </div>

                        <div class="post-actions" v-if="isMe">
                            <button class="btn-delete-post" @click.stop="deletePost(post.id)">
                                Delete Post
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- My profile actions -->
            <!-- My profile actions -->
            <div class="profile-card" v-if="isMe">
                <h3>Update Your Profile</h3>

                <div class="profile-field">
                    <label>Email:</label>
                    <input v-model="email" placeholder="Email" />
                </div>

                <div class="profile-field">
                    <label>Name:</label>
                    <input v-model="name" placeholder="Name" />
                </div>

                <button @click="updateMe" class="btn-save">Save Profile</button>

                <h3>Change Password</h3>

                <div class="profile-field">
                    <input v-model="oldPassword" type="password" placeholder="Old Password" />
                </div>
                <div class="profile-field">
                    <input v-model="newPassword" type="password" placeholder="New Password" />
                </div>

                <button @click="changePassword" class="btn-save">Change Password</button>

                <h3>Emails</h3>
                <ul>
                    <li v-for="emailItem in myEmails" :key="emailItem.id">{{ emailItem.subject }}</li>
                </ul>
            </div>


        </template>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '~/stores/user-store'
import { useAuthStore } from '~/stores/auth-store'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const authStore = useAuthStore()

const user = ref({})
const posts = ref([])
const loading = ref(false)
const error = ref(null)

const email = ref('')
const name = ref('')
const role = ref('')
const oldPassword = ref('')
const newPassword = ref('')

const myEmails = ref([])

async function loadEmails() {
    try {
        myEmails.value = await userStore.getMyEmails()
    } catch (e) {
        console.error('Failed to load emails', e)
    }
}

const isMe = computed(() =>
    route.params.username === authStore.user?.username
)

const isAdmin = computed(() =>
    authStore.user?.role === 'ADMINISTRADOR'
)

const canAdminEdit = computed(() =>
    isAdmin.value && !isMe.value
)

async function loadPosts() {
    if (isMe.value) {
        posts.value = await userStore.getMyPosts()
    } else {
        posts.value = await userStore.getUserPosts(route.params.username)
    }
}


async function loadUserProfile() {
    if (isMe.value) {
        user.value = await userStore.getMe()
    } else {
        user.value = await userStore.getByUsername(route.params.username)
    }
}

onMounted(async () => {
    loading.value = true
    error.value = null

    try {
        await loadUserProfile()
        role.value = user.value.role
        email.value = user.value.email || ''
        name.value = user.value.name || ''
        await loadPosts()

        if (isMe.value) {
            await loadEmails()
        }
    } catch (e) {
        error.value = 'Failed to load user profile'
        console.error(e)
    } finally {
        loading.value = false
    }
})




async function changeRole() {
    try {
        await userStore.changeRole(user.value.username, role.value)
        user.value.role = role.value
    } catch (e) {
        error.value = 'Failed to change role'
        console.error(e)
    }
}

async function toggleStatus() {
    try {
        await userStore.changeStatus(user.value.username, !user.value.blocked)
        user.value.blocked = !user.value.blocked
    } catch (e) {
        error.value = 'Failed to change status'
        console.error(e)
    }
}

async function adminUpdateUser() {
    try {
        await userStore.update(user.value.username, {
            email: email.value,
            name: name.value
        })

        user.value.email = email.value
        user.value.name = name.value

        alert('User updated successfully')
    } catch (e) {
        error.value = 'Failed to update user'
        console.error(e)
    }
}


async function updateMe() {
    try {
        await userStore.updateMe({ email: email.value, name: name.value })
        user.value.email = email.value
        user.value.name = name.value
        alert('Profile updated successfully')
    } catch (e) {
        error.value = 'Failed to update profile'
        console.error(e)
    }
}

async function changePassword() {
    if (!oldPassword.value || !newPassword.value) {
        alert('Please fill in both password fields')
        return
    }

    try {
        await userStore.changeMyPassword(oldPassword.value, newPassword.value)
        oldPassword.value = ''
        newPassword.value = ''
        alert('Password changed successfully')
    } catch (e) {
        error.value = 'Failed to change password'
        console.error(e)
    }
}

async function deletePost(id) {
    if (!confirm('Delete this post?')) return

    try {
        await userStore.deleteMyPost(id)
        posts.value = posts.value.filter(p => p.id !== id)
    } catch (e) {
        error.value = 'Failed to delete post'
        console.error(e)
    }
}

function goToPost(postId) {
    router.push(`/publication/${postId}`)
}

function goToHistory() {
    router.push(`/users/${route.params.username}-history`)
}

function goBack() {
    router.push(`/users`)
}
</script>

<style scoped>
.user-profile {
    max-width: 900px;
    margin: 0 auto;
    padding: 20px;
}

.profile-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

.btn-history {
    background: #28a745;
    color: white;
}

.profile-header h1 {
    margin-right: 550px;
}

/* Profile card */
.profile-card {
    background: white;
    padding: 20px;
    border-radius: 6px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    margin-bottom: 30px;
}

.profile-field {
    margin-bottom: 12px;
    font-size: 14px;
}

.profile-field span {
    font-weight: 600;
}

/* Admin controls */
.admin-controls {
    display: flex;
    gap: 10px;
    margin-top: 15px;
    flex-wrap: wrap;
}

.select-role {
    padding: 6px 10px;
    border-radius: 4px;
    border: 1px solid #ddd;
}

.btn-back {
    background: #6c757d;
    color: white;
    padding: 8px 16px;
    border-radius: 4px;
    border: none;
    cursor: pointer;
    font-size: 14px;

}

.btn-back:hover {
    background: #5a6268;
}

.btn-save {
    background: #007bff;
    color: white;
}

.btn-save:hover {
    background: #0056b3;
}

/* Posts */
.posts-section {
    margin-bottom: 30px;
}

.posts-section h2 {
    margin-bottom: 15px;
}

.posts-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.post-card {
    background: white;
    padding: 14px;
    border-radius: 6px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.post-title {
    font-weight: 600;
    margin-bottom: 6px;
    transition: color 0.2s;
}

.post-title:hover {
    color: #0077cc;
}

.post-actions {
    display: flex;
    gap: 8px;
    margin-top: 10px;
}

.btn-delete-post {
    background: #dc3545;
    color: white;
}

.btn-delete-post:hover {
    background: #c82333;
}

/* Inputs */
input {
    padding: 8px 12px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 14px;
}

input:focus {
    outline: none;
    border-color: #007bff;
}

/* Buttons generic */
button {
    border: none;
    padding: 8px 14px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
}

button:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

/* States */
.loading,
.empty,
.error {
    text-align: center;
    padding: 40px;
    color: #666;
}

.error {
    background: #fee;
    color: #dc3545;
    border-radius: 6px;
    margin-bottom: 20px;
}
</style>