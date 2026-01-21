<template>
  <div class="user-history">
    <button @click="goBackToUser" class="btn-back-to-user">← Back to User Details</button>
    <h1>User History</h1>

    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="historyItems.length === 0" class="empty">
      <p>No history found for this user</p>
    </div>
    <div v-else class="history-list">
      <div v-for="(item, index) in historyItems" :key="index" class="history-item">
        <div class="history-header">
          <span class="action-badge" :class="getActionClass(item.action)">{{ item.action }}</span>
          <span class="timestamp">{{ formatTimestamp(item.timestamp) }}</span>
        </div>
        <p class="description">{{ item.description }}</p>
        <div v-if="item.user" class="history-footer">
          <span class="user">By: {{ item.user }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '~/stores/auth-store'

const route = useRoute()
const router = useRouter()
const config = useRuntimeConfig()
const api = config.public.apiBase
const authStore = useAuthStore()

function goBackToUser() {
  const userId = route.query.id
  if (userId) {
    router.push({ path: '/users/userDetails', query: { id: userId } })
  } else {
    router.push('/users/users')
  }
}

const historyItems = ref([])
const loading = ref(true)
const error = ref(null)

useAuthErrorRedirect(error)

onMounted(async () => {
  const username = route.query.username

  if (!username) {
    error.value = 'Username is required'
    loading.value = false
    return
  }

  try {
    if (!authStore.token) {
      throw new Error('Not authenticated')
    }

    const response = await $fetch(`${api}/history/${username}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Accept': 'application/json'
      }
    })

    if (Array.isArray(response)) {
      historyItems.value = response
    } else if (response && response.action) {
      historyItems.value = [response]
    } else {
      historyItems.value = []
    }
  } catch (err) {
    error.value = err.message || 'Failed to fetch user history'
  } finally {
    loading.value = false
  }
})

function formatTimestamp(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

function getActionClass(action) {
  if (!action) return ''
  const actionLower = String(action).toLowerCase()
  if (actionLower.includes('created')) return 'action-created'
  if (actionLower.includes('updated')) return 'action-updated'
  if (actionLower.includes('deleted')) return 'action-deleted'
  return 'action-default'
}
</script>

<style scoped>
.user-history {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.btn-back-to-user {
  padding: 10px 20px;
  background: #0077cc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 20px;
  transition: background 0.2s;
}

.btn-back-to-user:hover {
  background: #005fa3;
}

h1 {
  color: #333;
  margin-bottom: 30px;
}

.loading,
.error,
.empty {
  text-align: center;
  padding: 40px;
  font-size: 16px;
}

.error {
  color: #c33;
  background: #fee;
  border-radius: 4px;
}

.empty {
  color: #666;
}

.history-list {
  display: grid;
  gap: 20px;
}

.history-item {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 10px;
}

.action-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.action-created {
  background: #d4edda;
  color: #155724;
}

.action-updated {
  background: #d1ecf1;
  color: #0c5460;
}

.action-deleted {
  background: #f8d7da;
  color: #721c24;
}

.action-default {
  background: #e2e3e5;
  color: #383d41;
}

.timestamp {
  color: #666;
  font-size: 13px;
  font-style: italic;
}

.description {
  color: #333;
  font-size: 15px;
  line-height: 1.6;
  margin: 0;
}
</style>

