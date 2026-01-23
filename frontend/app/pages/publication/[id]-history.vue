<template>
    <div class="publication-history">
      <button @click="goToPublications" class="btn-back-to-pubs">← Back to Publications</button>
      <h1>Publication History</h1>
      <div v-if="loading" class="loading">Loading...</div>
      <div v-else-if="error" class="error">{{ error }}</div>
      <div v-else-if="publicationHistory.length === 0" class="empty">
        <p>No publication history found</p>
      </div>
      <div v-else class="history-list">
        <div v-for="(item, index) in publicationHistory" :key="index" class="history-item">
          <div class="history-header">
            <span class="action-badge" :class="getActionClass(item.action)">{{ item.action }}</span>
            <span class="timestamp">{{ formatTimestamp(item.timestamp) }}</span>
          </div>
          <p class="description">{{ item.description }}</p>
          <div class="history-footer">
            <span class="user">By: {{ item.user }}</span>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { usePublicationStore } from '~/stores/publication-store'
  
  const route = useRoute()
  const router = useRouter()
  const publicationStore = usePublicationStore()
  
  function goToPublications() {
    router.push('/publication/searchPublications')
  }
  
  const publicationHistory = ref([])
  const loading = ref(true)
  const error = ref(null)
  
  const publicationId = computed(() => {
    const param = route.params['id-history'] || route.params.id

    return typeof param === 'string' ? param.replace(/-history$/, '') : param
})

const isAdmin = computed(() =>
    authStore.user?.role === 'ADMINISTRADOR'
)

  onMounted(async () => {
    const pubId = publicationId.value
    
    if (!pubId) {
      error.value = 'Publication ID is required'
      loading.value = false
      return
    }
  
    try {
      
      publicationHistory.value = await publicationStore.getHistory(pubId)
      console.log(publicationHistory.value)
    } catch (err) {
      error.value = err.message || 'Failed to fetch publication history'
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
    const actionLower = action.toLowerCase()
    if (actionLower.includes('created')) return 'action-created'
    if (actionLower.includes('updated')) return 'action-updated'
    if (actionLower.includes('deleted')) return 'action-deleted'
    return 'action-default'
  }
  </script>
  
  <style scoped>
  .publication-history {
    padding: 20px;
    max-width: 1200px;
    margin: 0 auto;
  }
  
  .btn-back-to-pubs {
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
  
  .btn-back-to-pubs:hover {
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
    transition: transform 0.2s, box-shadow 0.2s;
  }
  
  .history-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
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
    margin: 0 0 12px 0;
  }
  
  .history-footer {
    display: flex;
    justify-content: flex-end;
    padding-top: 12px;
    border-top: 1px solid #eee;
  }
  
  .user {
    color: #666;
    font-size: 13px;
    font-weight: 500;
  }
</style>