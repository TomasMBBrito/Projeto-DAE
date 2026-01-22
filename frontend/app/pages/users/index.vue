<template>
    <div class="users-page">
        <div class="page-header">
            <h1>User Management</h1>
            <button @click="goBack" class="btn-back">← Back to Publications</button>
        </div>
        
        <!-- Admin tools -->
        <div v-if="isAdmin" class="admin-section">
            <div class="search-container">
                <input 
                    v-model="search" 
                    placeholder="Search by username..." 
                    @keyup.enter="onSearch"
                    class="search-input"
                />
                <button @click="onSearch" class="btn-search">Search</button>
            </div>
            <button @click="goCreate" class="btn-create">+ Create User</button>
        </div>
        
        <div class="loading" v-if="loading">Loading users...</div>
        <div class="error" v-else-if="error">{{ error }}</div>
        <div class="empty" v-else-if="displayUsers.length === 0">No users found</div>
        
        <div v-else class="users-grid">
            <div v-for="user in displayUsers" :key="user.username" class="user-card">
                <div 
                    @click="canViewProfiles ? goProfile(user.username) : null" 
                    class="user-content"
                    :class="{ 'clickable': canViewProfiles }"
                >
                    <div class="user-avatar">
                        {{ user.username.charAt(0).toUpperCase() }}
                    </div>
                    <div class="user-details">
                        <div class="user-name">{{ user.username }}</div>
                        <div class="user-role" :class="`role-${user.role.toLowerCase()}`">
                            {{ user.role }}
                        </div>
                    </div>
                </div>
                <div v-if="isAdmin" class="user-actions">
                    <button class="btn-action btn-edit" @click.stop="editUser(user)">
                        Edit
                    </button>
                    <button class="btn-action btn-delete" @click.stop="deleteUser(user.username)">
                        Delete
                    </button>
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
const isSearching = ref(false)

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

const displayUsers = computed(() => {
    if (isSearching.value && searchResult.value) {
        return [searchResult.value]
    }
    return users.value
})

async function onSearch() {
    if (!search.value.trim()) {
        searchResult.value = null
        isSearching.value = false
        return
    }
    
    loading.value = true
    error.value = null
    isSearching.value = true
    
    try {
        const user = await userStore.getByUsername(search.value.trim())
        searchResult.value = user
        error.value = null
    } catch (e) {
        searchResult.value = null
        error.value = `User "${search.value}" not found`
    } finally {
        loading.value = false
    }
}

function goProfile(username) {
    router.push(`/users/${username}`)
}

function goCreate() {
    router.push('/users/create')
}

async function deleteUser(username) {
    if (!confirm(`Are you sure you want to delete user "${username}"?`)) return
    try {
        await userStore.remove(username)
        users.value = users.value.filter(u => u.username !== username)
        if (searchResult.value?.username === username) {
            searchResult.value = null
            search.value = ''
            isSearching.value = false
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
.users-page {
    font-family: "Inter", sans-serif;
    max-width: 1200px;
    margin: 0 auto;
    padding: 30px 20px;
    background: #f9fafb;
    min-height: 100vh;
}

/* Page Header */
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 35px;
    padding-bottom: 20px;
    border-bottom: 2px solid #e2e8f0;
}

h1 {
    margin: 0;
    color: #1e293b;
    font-size: 32px;
    font-weight: 700;
}

.btn-back {
    background: #3b82f6;
    color: white;
    border: none;
    padding: 10px 20px;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 600;
    font-size: 14px;
    transition: all 0.2s;
}

.btn-back:hover {
    background: #2563eb;
    transform: translateY(-1px);
}

/* Admin Section */
.admin-section {
    background: white;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 25px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    display: flex;
    gap: 12px;
    align-items: center;
}

.search-container {
    flex: 1;
    display: flex;
    gap: 10px;
}

.search-input {
    flex: 1;
    padding: 12px 16px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    font-size: 14px;
    transition: all 0.2s;
}

.search-input:focus {
    outline: none;
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.btn-search {
    background: #3b82f6;
    color: white;
    border: none;
    padding: 12px 24px;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 600;
    font-size: 14px;
    transition: all 0.2s;
}

.btn-search:hover {
    background: #2563eb;
}

.btn-create {
    background: #10b981;
    color: white;
    border: none;
    padding: 12px 24px;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 600;
    font-size: 14px;
    transition: all 0.2s;
    white-space: nowrap;
}

.btn-create:hover {
    background: #059669;
}

/* Users Grid */
.users-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 20px;
}

.user-card {
    background: white;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    transition: all 0.2s;
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.user-card:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
}

.user-content {
    display: flex;
    align-items: center;
    gap: 15px;
    flex: 1;
}

.user-content.clickable {
    cursor: pointer;
}

.user-content.clickable:hover .user-name {
    color: #3b82f6;
}

.user-avatar {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    font-weight: 700;
    flex-shrink: 0;
}

.user-details {
    display: flex;
    flex-direction: column;
    gap: 4px;
    flex: 1;
    min-width: 0;
}

.user-name {
    font-weight: 600;
    font-size: 16px;
    color: #1e293b;
    transition: color 0.2s;
}

.user-role {
    font-size: 13px;
    font-weight: 500;
    padding: 4px 10px;
    border-radius: 12px;
    display: inline-block;
    width: fit-content;
}

.role-administrador {
    background: #fef3c7;
    color: #92400e;
}

.role-responsavel {
    background: #dbeafe;
    color: #1e40af;
}

.role-colaborador {
    background: #d1fae5;
    color: #065f46;
}

.role-utilizador {
    background: #e5e7eb;
    color: #374151;
}

/* User Actions */
.user-actions {
    display: flex;
    gap: 8px;
    padding-top: 10px;
    border-top: 1px solid #e2e8f0;
}

.btn-action {
    flex: 1;
    border: none;
    padding: 8px 16px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 13px;
    font-weight: 600;
    transition: all 0.2s;
}

.btn-edit {
    background: #10b981;
    color: white;
}

.btn-edit:hover {
    background: #059669;
}

.btn-delete {
    background: #ef4444;
    color: white;
}

.btn-delete:hover {
    background: #dc2626;
}

/* States */
.loading,
.empty,
.error {
    text-align: center;
    padding: 60px 20px;
    color: #64748b;
    font-size: 16px;
}

.error {
    background: #fef2f2;
    color: #dc2626;
    border-radius: 8px;
    padding: 30px;
    border: 1px solid #fecaca;
}

.empty {
    background: white;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
}

/* Responsive */
@media (max-width: 768px) {
    .users-grid {
        grid-template-columns: 1fr;
    }
    
    .admin-section {
        flex-direction: column;
        align-items: stretch;
    }
    
    .btn-create {
        width: 100%;
    }
    
    h1 {
        font-size: 24px;
    }
}
</style>