<template>
    <div class="user-profile">
        <div class="loading" v-if="loading">Loading profile...</div>
        <div class="error" v-else-if="error">{{ error }}</div>

        <template v-else>
            <!-- Header -->
            <div class="profile-header">
                <div class="header-left">
                    <div class="user-avatar-large">
                        {{ user.username?.charAt(0).toUpperCase() }}
                    </div>
                    <div class="header-info">
                        <h1>{{ user.username }}</h1>
                        <span class="user-role-badge" :class="`role-${user.role?.toLowerCase()}`">
                            {{ user.role }}
                        </span>
                    </div>
                </div>
                <div class="header-actions">
                    <button @click="goToHistory" class="btn-secondary">View History</button>
                    <button @click="goBack" class="btn-back">← Back to Users</button>
                </div>
            </div>

            <!-- Profile Information Card -->
            <div class="info-section">
                <div class="section-title">Profile Information</div>
                <div class="info-grid">
                    <div class="info-item">
                        <div class="info-label">Email</div>
                        <div class="info-value">{{ user.email }}</div>
                    </div>
                    <div class="info-item" v-if="user.name">
                        <div class="info-label">Name</div>
                        <div class="info-value">{{ user.name }}</div>
                    </div>
                    <div class="info-item">
                        <div class="info-label">Status</div>
                        <div class="info-value">
                            <span class="status-badge" :class="user.blocked ? 'blocked' : 'active'">
                                {{ user.blocked ? 'Blocked' : 'Active' }}
                            </span>
                        </div>
                    </div>
                </div>

                <!-- Admin Controls -->
                <div v-if="isAdmin && !isMe" class="admin-controls">
                    <div class="control-group">
                        <label>Change Role</label>
                        <select v-model="role" @change="changeRole" class="select-role">
                            <option>COLABORADOR</option>
                            <option>RESPONSAVEL</option>
                            <option>ADMINISTRADOR</option>
                        </select>
                    </div>
                    <button @click="toggleStatus" class="btn-toggle-status" :class="user.blocked ? 'btn-activate' : 'btn-block'">
                        {{ user.blocked ? 'Activate Account' : 'Block Account' }}
                    </button>
                </div>
            </div>

            <!-- Admin Edit User -->
            <div class="settings-section" v-if="isAdmin && !isMe">
                <div class="section-title">Edit User Information</div>
                
                <div class="settings-card">
                    <h3>Update User Details</h3>
                    <div class="form-group">
                        <label>Email</label>
                        <input v-model="email" type="email" placeholder="Enter email" />
                    </div>
                    <div class="form-group">
                        <label>Name</label>
                        <input v-model="name" type="text" placeholder="Enter name" />
                    </div>
                    <button @click="adminUpdateUser" class="btn-primary">Save Changes</button>
                </div>
            </div>

            <!-- Posts Section -->
            <div class="posts-section">
                <div class="section-title">Publications</div>
                
                <div class="empty-state" v-if="posts.length === 0">
                    <div class="empty-icon">📄</div>
                    <p>No publications yet</p>
                </div>

                <div v-else class="posts-grid">
                    <div v-for="post in posts" :key="post.id" class="post-card">
                        <div class="post-content" @click="goToPost(post.id)">
                            <div class="post-title">{{ post.title }}</div>
                        </div>
                        <div class="post-actions" v-if="isMe">
                            <button class="btn-delete-post" @click.stop="deletePost(post.id)">
                                Delete
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- My Profile Settings -->
            <div class="settings-section" v-if="isMe">
                <div class="section-title">Account Settings</div>
                
                <!-- Update Profile -->
                <div class="settings-card">
                    <h3>Update Profile</h3>
                    <div class="form-group">
                        <label>Email</label>
                        <input v-model="email" type="email" placeholder="Enter your email" />
                    </div>
                    <div class="form-group">
                        <label>Name</label>
                        <input v-model="name" type="text" placeholder="Enter your name" />
                    </div>
                    <button @click="updateMe" class="btn-primary">Save Profile</button>
                </div>

                <!-- Change Password -->
                <div class="settings-card">
                    <h3>Change Password</h3>
                    <div class="form-group">
                        <label>Current Password</label>
                        <input v-model="oldPassword" type="password" placeholder="Enter current password" />
                    </div>
                    <div class="form-group">
                        <label>New Password</label>
                        <input v-model="newPassword" type="password" placeholder="Enter new password" />
                    </div>
                    <button @click="changePassword" class="btn-primary">Update Password</button>
                </div>
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
    if (!email.value || !name.value) {
        alert('Please fill in all fields')
        return
    }

    console.log('Updating user:', user.value.username, { email: email.value, name: name.value })

    try {
        const response = await userStore.update(user.value.username, {
            email: email.value,
            name: name.value
        })

        console.log('Update response:', response)

        // Update local user object
        user.value.email = email.value
        user.value.name = name.value

        alert('User updated successfully')
    } catch (e) {
        console.error('Update error:', e)
        error.value = 'Failed to update user: ' + (e.data?.message || e.message || 'Unknown error')
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
    font-family: "Inter", sans-serif;
    max-width: 1100px;
    margin: 0 auto;
    padding: 30px 20px;
    background: #f9fafb;
    min-height: 100vh;
}

/* Profile Header */
.profile-header {
    background: white;
    padding: 30px;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    margin-bottom: 25px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 20px;
}

.user-avatar-large {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    font-weight: 700;
    flex-shrink: 0;
}

.header-info h1 {
    margin: 0 0 8px 0;
    color: #1e293b;
    font-size: 28px;
    font-weight: 700;
}

.user-role-badge {
    display: inline-block;
    padding: 6px 14px;
    border-radius: 12px;
    font-size: 13px;
    font-weight: 600;
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

.header-actions {
    display: flex;
    gap: 10px;
}

.btn-back,
.btn-secondary {
    padding: 10px 20px;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 600;
    font-size: 14px;
    transition: all 0.2s;
}

.btn-back {
    background: #64748b;
    color: white;
}

.btn-back:hover {
    background: #475569;
}

.btn-secondary {
    background: #10b981;
    color: white;
}

.btn-secondary:hover {
    background: #059669;
}

/* Section Styling */
.info-section,
.posts-section,
.settings-section {
    background: white;
    padding: 25px;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    margin-bottom: 25px;
}

.section-title {
    font-size: 20px;
    font-weight: 700;
    color: #1e293b;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 2px solid #e2e8f0;
}

/* Info Grid */
.info-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
    margin-bottom: 20px;
}

.info-item {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.info-label {
    font-size: 13px;
    font-weight: 600;
    color: #64748b;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.info-value {
    font-size: 16px;
    color: #1e293b;
    font-weight: 500;
}

.status-badge {
    display: inline-block;
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 13px;
    font-weight: 600;
}

.status-badge.active {
    background: #d1fae5;
    color: #065f46;
}

.status-badge.blocked {
    background: #fee2e2;
    color: #991b1b;
}

/* Admin Controls */
.admin-controls {
    display: flex;
    gap: 15px;
    align-items: flex-end;
    padding-top: 20px;
    border-top: 1px solid #e2e8f0;
}

.control-group {
    display: flex;
    flex-direction: column;
    gap: 6px;
    flex: 1;
    max-width: 250px;
}

.control-group label {
    font-size: 13px;
    font-weight: 600;
    color: #64748b;
}

.select-role {
    padding: 10px 14px;
    border-radius: 6px;
    border: 1px solid #e2e8f0;
    font-size: 14px;
    background: white;
    cursor: pointer;
}

.select-role:focus {
    outline: none;
    border-color: #3b82f6;
}

.btn-toggle-status {
    padding: 10px 20px;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 600;
    font-size: 14px;
    transition: all 0.2s;
}

.btn-activate {
    background: #10b981;
    color: white;
}

.btn-activate:hover {
    background: #059669;
}

.btn-block {
    background: #ef4444;
    color: white;
}

.btn-block:hover {
    background: #dc2626;
}

/* Posts Grid */
.posts-grid {
    display: grid;
    gap: 12px;
}

.post-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    background: #f9fafb;
    border-radius: 8px;
    border-left: 3px solid #3b82f6;
    transition: all 0.2s;
}

.post-card:hover {
    background: #f1f5f9;
    transform: translateX(4px);
}

.post-content {
    flex: 1;
    cursor: pointer;
}

.post-title {
    font-weight: 600;
    color: #1e293b;
    font-size: 15px;
}

.post-content:hover .post-title {
    color: #3b82f6;
}

.post-actions {
    display: flex;
    gap: 8px;
}

.btn-delete-post {
    background: #ef4444;
    color: white;
    border: none;
    padding: 6px 14px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 13px;
    font-weight: 600;
    transition: all 0.2s;
}

.btn-delete-post:hover {
    background: #dc2626;
}

/* Settings Section */
.settings-card {
    background: #f9fafb;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
}

.settings-card:last-child {
    margin-bottom: 0;
}

.settings-card h3 {
    margin: 0 0 18px 0;
    color: #1e293b;
    font-size: 18px;
    font-weight: 600;
}

.form-group {
    margin-bottom: 16px;
}

.form-group label {
    display: block;
    margin-bottom: 6px;
    font-size: 13px;
    font-weight: 600;
    color: #64748b;
}

.form-group input {
    width: 100%;
    padding: 10px 14px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    font-size: 14px;
    transition: all 0.2s;
}

.form-group input:focus {
    outline: none;
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.btn-primary {
    background: #3b82f6;
    color: white;
    border: none;
    padding: 10px 24px;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 600;
    font-size: 14px;
    transition: all 0.2s;
}

.btn-primary:hover {
    background: #2563eb;
}

/* Emails List */
.emails-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.email-item {
    padding: 12px;
    background: white;
    border-radius: 6px;
    border-left: 3px solid #3b82f6;
    font-size: 14px;
    color: #1e293b;
}

/* Empty State */
.empty-state {
    text-align: center;
    padding: 60px 20px;
    color: #64748b;
}

.empty-icon {
    font-size: 48px;
    margin-bottom: 12px;
}

.empty-state p {
    margin: 0;
    font-size: 16px;
}

/* States */
.loading,
.error {
    text-align: center;
    padding: 60px 20px;
    color: #64748b;
    font-size: 16px;
}

.error {
    background: #fef2f2;
    color: #dc2626;
    border-radius: 12px;
    padding: 30px;
    border: 1px solid #fecaca;
}

/* Responsive */
@media (max-width: 768px) {
    .profile-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 20px;
    }

    .header-actions {
        width: 100%;
        flex-direction: column;
    }

    .header-actions button {
        width: 100%;
    }

    .admin-controls {
        flex-direction: column;
        align-items: stretch;
    }

    .control-group {
        max-width: 100%;
    }
}
</style>