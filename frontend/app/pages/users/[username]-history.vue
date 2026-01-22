<template>
    <div class="user-history">
        <div class="loading" v-if="loading">Loading history...</div>
        <div class="error" v-else-if="error">
            <div class="error-icon">⚠</div>
            <div class="error-content">{{ error }}</div>
        </div>
        
        <template v-else>
            <div class="page-header">
                <div class="header-left">
                    <div class="user-avatar">
                        {{ username?.charAt(0).toUpperCase() }}
                    </div>
                    <div class="header-info">
                        <h1>Activity History</h1>
                        <p class="username">{{ username }}</p>
                    </div>
                </div>
                <button @click="goBack" class="btn-back">← Back to Profile</button>
            </div>

            <div class="history-section">
                <div class="section-header">
                    <h2>Recent Activity</h2>
                    <span class="history-count">{{ history.length }} {{ history.length === 1 ? 'entry' : 'entries' }}</span>
                </div>

                <div class="empty-state" v-if="history.length === 0">
                    <div class="empty-icon">📋</div>
                    <p>No activity history available</p>
                </div>
                
                <div v-else class="history-timeline">
                    <div v-for="(item, index) in history" :key="index" class="history-item">
                        <div class="timeline-marker"></div>
                        <div class="history-card">
                            <div class="history-header">
                                <span class="history-action" :class="getActionClass(item.action)">
                                    {{ formatAction(item.action) }}
                                </span>
                                <span class="history-timestamp">{{ formatTimestamp(item.timestamp) }}</span>
                            </div>
                            <div class="history-description">{{ item.description }}</div>
                            <div class="history-footer" v-if="item.user">
                                <span class="history-user">Performed by: {{ item.user }}</span>
                            </div>
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

const username = computed(() => {
    const param = route.params['username-history'] || route.params.username
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
        return date.toLocaleString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        })
    } catch (e) {
        return timestamp
    }
}

function formatAction(action) {
    if (!action) return ''
    return action
        .toLowerCase()
        .split('_')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ')
}

function getActionClass(action) {
    if (!action) return ''
    const actionLower = action.toLowerCase()
    
    if (actionLower.includes('create') || actionLower.includes('add')) {
        return 'action-create'
    } else if (actionLower.includes('delete') || actionLower.includes('remove') || actionLower.includes('hidden')) {
        return 'action-delete'
    } else if (actionLower.includes('update') || actionLower.includes('edit') || actionLower.includes('change')) {
        return 'action-update'
    } else if (actionLower.includes('login') || actionLower.includes('logout')) {
        return 'action-auth'
    }
    
    return 'action-default'
}

function goBack() {
    router.push(`/users/${username.value}`)
}
</script>

<style scoped>
.user-history {
    font-family: "Inter", sans-serif;
    max-width: 1000px;
    margin: 0 auto;
    padding: 30px 20px;
    background: #f9fafb;
    min-height: 100vh;
}

/* Page Header */
.page-header {
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

.user-avatar {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    font-weight: 700;
    flex-shrink: 0;
}

.header-info h1 {
    margin: 0 0 4px 0;
    color: #1e293b;
    font-size: 28px;
    font-weight: 700;
}

.username {
    margin: 0;
    color: #64748b;
    font-size: 14px;
}

.btn-back {
    background: #64748b;
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
    background: #475569;
}

/* History Section */
.history-section {
    background: white;
    padding: 30px;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 25px;
    padding-bottom: 15px;
    border-bottom: 2px solid #e2e8f0;
}

.section-header h2 {
    margin: 0;
    color: #1e293b;
    font-size: 20px;
    font-weight: 700;
}

.history-count {
    color: #64748b;
    font-size: 14px;
    font-weight: 500;
}

/* Timeline */
.history-timeline {
    position: relative;
}

.history-item {
    position: relative;
    margin-bottom: 20px;
}

.history-item:last-child {
    margin-bottom: 0;
}

.timeline-marker {
    display: none;
}

/* History Card */
.history-card {
    background: #f9fafb;
    padding: 18px;
    border-radius: 8px;
    border-left: 3px solid #3b82f6;
    transition: all 0.2s;
}

.history-card:hover {
    background: #f1f5f9;
    transform: translateX(4px);
}

.history-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    gap: 12px;
}

.history-action {
    font-weight: 600;
    font-size: 15px;
    padding: 4px 12px;
    border-radius: 12px;
    display: inline-block;
}

.action-create {
    background: #d1fae5;
    color: #065f46;
}

.action-delete {
    background: #fee2e2;
    color: #991b1b;
}

.action-update {
    background: #dbeafe;
    color: #1e40af;
}

.action-auth {
    background: #fef3c7;
    color: #92400e;
}

.action-default {
    background: #e5e7eb;
    color: #374151;
}

.history-timestamp {
    color: #64748b;
    font-size: 13px;
    white-space: nowrap;
}

.history-description {
    font-size: 14px;
    color: #1e293b;
    line-height: 1.5;
    margin-bottom: 8px;
}

.history-footer {
    margin-top: 8px;
    padding-top: 8px;
    border-top: 1px solid #e2e8f0;
}

.history-user {
    color: #64748b;
    font-size: 12px;
    font-style: italic;
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
.loading {
    text-align: center;
    padding: 60px 20px;
    color: #64748b;
    font-size: 16px;
}

.error {
    display: flex;
    align-items: center;
    gap: 12px;
    background: #fef2f2;
    color: #dc2626;
    border-radius: 12px;
    padding: 30px;
    border: 1px solid #fecaca;
}

.error-icon {
    font-size: 24px;
}

.error-content {
    flex: 1;
}

/* Responsive */
@media (max-width: 768px) {
    .page-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 20px;
    }

    .btn-back {
        width: 100%;
    }

    .section-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
    }

    .history-header {
        flex-direction: column;
        align-items: flex-start;
    }

    .history-timestamp {
        font-size: 12px;
    }
}
</style>