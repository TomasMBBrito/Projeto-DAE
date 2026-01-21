<template>
    <div class="user-history">
        <div class="loading" v-if="loading">Loading...</div>
        <div class="error" v-else-if="error">{{ error }}</div>
        
        <template v-else>
            <div class="history-header">
                <h1>History - {{ username }}</h1>
                <button @click="goBack" class="btn-back">← Back to Profile</button>
            </div>

            <div class="history-section">
                <div class="empty" v-if="history.length === 0">No history available</div>
                
                <div v-else class="history-list">
                    <div v-for="(item, index) in history" :key="index" class="history-item">
                        <div class="history-action">{{ formatAction(item.action) }}</div>
                        <div class="history-description">{{ item.description }}</div>
                        <div class="history-meta">
                            <span class="history-timestamp">{{ formatTimestamp(item.timestamp) }}</span>
                            <span class="history-user" v-if="item.user">• User: {{ item.user }}</span>
                        </div>
                    </div>
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

const history = ref([])
const loading = ref(false)
const error = ref(null)

// Extract username from route - Nuxt will create route param 'username-history' 
// which contains the full value like 'joao-history', so we extract the username
const username = computed(() => {
    const param = route.params['username-history'] || route.params.username
    // Remove '-history' suffix if present
    return typeof param === 'string' ? param.replace(/-history$/, '') : param
})

const isMe = computed(() => {
    const user = username.value
    return user === authStore.user?.username
})

const isAdmin = computed(() =>
    authStore.user?.role === 'ADMINISTRADOR'
)

onMounted(async () => {
    loading.value = true
    error.value = null
    
    try {
        const user = username.value
        // Use my history if viewing own profile, otherwise use admin endpoint
        if (isMe.value) {
            history.value = await userStore.getMyHistory()
        } else if (isAdmin.value) {
            history.value = await userStore.getUserHistory(user)
        } else {
            error.value = 'You do not have permission to view this history'
        }
    } catch (e) {
        error.value = 'Failed to load user history'
        console.error(e)
    } finally {
        loading.value = false
    }
})

function formatTimestamp(timestamp) {
    if (!timestamp) return ''
    try {
        const date = new Date(timestamp)
        return date.toLocaleString()
    } catch (e) {
        return timestamp
    }
}

function formatAction(action) {
    if (!action) return ''
    // Convert SCREAMING_SNAKE_CASE to Title Case
    // e.g., "COMMENT_HIDDEN" -> "Comment Hidden"
    return action
        .toLowerCase()
        .split('_')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ')
}

function goBack() {
    router.push(`/users/${username.value}`)
}
</script>

<style scoped>
.user-history {
    max-width: 900px;
    margin: 0 auto;
    padding: 20px;
}

.history-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

.history-header h1 {
    margin: 0;
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

.history-section {
    margin-bottom: 30px;
}

.history-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.history-item {
    background: white;
    padding: 16px;
    border-radius: 6px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.history-action {
    font-weight: 600;
    font-size: 16px;
    margin-bottom: 8px;
    color: #007bff;
}

.history-description {
    font-size: 14px;
    color: #333;
    margin-bottom: 8px;
}

.history-meta {
    display: flex;
    gap: 8px;
    align-items: center;
    margin-top: 8px;
    font-size: 12px;
}

.history-timestamp {
    color: #666;
}

.history-user {
    color: #888;
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
