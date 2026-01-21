<template>
    <div class="users-page">
        <div class="users-header">
            <h1>Users</h1>
            <button @click="goBack" class="btn-back">← Back to Publications</button>
        </div>
        
        <!-- Admin tools -->
        <div v-if="isAdmin" class="admin-tools">
            <input v-model="search" placeholder="Search username..." @input="onSearch" />
            <button @click="goCreate">+ Create User</button>
        </div>
        
        <div class="loading" v-if="loading">Loading...</div>
        <div class="error" v-else-if="error">{{ error }}</div>
        <div class="empty" v-else-if="displayUsers.length === 0">No users found</div>
        
        <div v-else class="users-list">
            <div v-for="user in displayUsers" :key="user.username" class="user-card">
                <div 
                    @click="canViewProfiles ? goProfile(user.username) : null" 
                    class="user-info"
                    :class="{ 'clickable': canViewProfiles }"
                >
                    <strong class="user-name">{{ user.username }}</strong>
                    <span class="user-role">{{ user.role }}</span>
                </div>
                <div v-if="isAdmin" class="user-actions">
                    <button class="btn-edit" @click.stop="editUser(user)">Edit</button>
                    <button class="btn-delete" @click.stop="deleteUser(user.username)">Delete</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '~/stores/user-store'
import { useAuthStore } from '~/stores/auth-store'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const authStore = useAuthStore()
const router = useRouter()

const users = ref([])
const search = ref('')
const searchResult = ref(null)
const loading = ref(false)
const error = ref(null)
let searchTimeout = null

const isAdmin = computed(() =>
    authStore.user?.role === 'ADMINISTRADOR'
)

const canViewProfiles = computed(() => {
    const role = authStore.user?.role
    return role === 'ADMINISTRADOR' || role === 'RESPONSAVEL' || role === 'COLABORADOR'
})

onMounted(async () => {
    loading.value = true
    try {
        users.value = await userStore.getAll()
    } catch (e) {
        error.value = 'Failed to load users'
        console.error(e)
    } finally {
        loading.value = false
    }
})

// Display either search result or all users
const displayUsers = computed(() => {
    if (search.value && searchResult.value) {
        return [searchResult.value]
    }
    return users.value
})

// Debounced search using the endpoint
async function onSearch() {
    if (searchTimeout) {
        clearTimeout(searchTimeout)
    }
    
    if (!search.value.trim()) {
        searchResult.value = null
        return
    }
    
    searchTimeout = setTimeout(async () => {
        loading.value = true
        error.value = null
        try {
            const user = await userStore.getByUsername(search.value.trim())
            searchResult.value = user
        } catch (e) {
            searchResult.value = null
            error.value = `User "${search.value}" not found`
        } finally {
            loading.value = false
        }
    }, 300) // 300ms debounce
}

function goProfile(username) {
    router.push(`/users/${username}`)
}

function goCreate() {
    router.push('/users/create')
}

async function deleteUser(username) {
    if (!confirm('Delete user?')) return
    try {
        await userStore.remove(username)
        users.value = users.value.filter(u => u.username !== username)
        if (searchResult.value?.username === username) {
            searchResult.value = null
            search.value = ''
        }
    } catch (e) {
        error.value = 'Failed to delete user'
        console.error(e)
    }
}

function editUser(user) {
    router.push(`/users/${user.username}`)
}

function goBack() {
    router.push(`/publication/searchPublications`)
}
</script>

<style scoped>

.users-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
}

.users-page {
    max-width: 1100px;
    margin: 0 auto;
    padding: 20px;
}

.admin-tools {
    margin-bottom: 20px;
    display: flex;
    gap: 10px;
}

.admin-tools input {
    flex: 1;
    padding: 8px 12px;
    border: 1px solid #ddd;
    border-radius: 4px;
}

.admin-tools input:focus {
    outline: none;
    border-color: #007bff;
}

.admin-tools button {
    background: #007bff;
    color: white;
    border: none;
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
    font-weight: 600;
}

.admin-tools button:hover {
    background: #0056b3;
}

/* Users list */
.users-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.user-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: white;
    padding: 14px 16px;
    border-radius: 6px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    transition: background 0.2s;
}

.user-card:has(.user-info.clickable:hover) {
    background: #f8f9fa;
}

.user-info {
    display: flex;
    flex-direction: column;
    flex: 1;
}

.user-info.clickable {
    cursor: pointer;
}

.user-name {
    font-weight: 600;
    font-size: 16px;
}

.user-role {
    font-size: 13px;
    color: #666;
}

/* Admin actions */
.user-actions {
    display: flex;
    gap: 8px;
}

.btn-edit {
    background: #28a745;
    color: white;
}

.btn-delete {
    background: #dc3545;
    color: white;
}

.btn-back {
    background: #007bff;
    color: white;
    border: none;
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
    font-weight: 600;
}
.btn-back:hover {
    background: #0056b3;
}

.btn-edit,
.btn-delete {
    border: none;
    padding: 6px 12px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 13px;
}

.btn-edit:hover {
    background: #218838;
}

.btn-delete:hover {
    background: #c82333;
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
    padding: 20px;
}
</style>