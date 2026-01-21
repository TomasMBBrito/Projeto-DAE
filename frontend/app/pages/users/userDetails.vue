<template>
  <div class="user-details">
    <h1>User Details</h1>
    
    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="user" class="user-info">
      <div class="user-card">
        <div class="user-header">
          <h2>{{ user.name }}</h2>
          <span class="status-badge" :class="{ 'blocked': user.blocked, 'active': !user.blocked }">
            {{ user.blocked ? 'Blocked' : 'Active' }}
          </span>
        </div>
        
        <div class="info-section">
          <h3 class="section-title">Basic Information</h3>
          <div class="info-item">
            <span class="label">Username:</span>
            <span class="value">{{ user.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">Email:</span>
            <span class="value">{{ user.email }}</span>
          </div>
          <div class="info-item">
            <span class="label">Name:</span>
            <span class="value">{{ user.name }}</span>
          </div>
          <div class="info-item">
            <span class="label">Role:</span>
            <span class="value role-badge">{{ user.role }}</span>
          </div>
          <div class="info-item">
            <span class="label">Status:</span>
            <span class="value">
              <span class="status-badge" :class="{ 'blocked': user.blocked, 'active': !user.blocked }">
                {{ user.blocked ? 'Blocked' : 'Active' }}
              </span>
            </span>
          </div>
        </div>

        <div class="info-section">
          <h3 class="section-title">Subscribed Tags</h3>
          <div v-if="user.subscribedTags && user.subscribedTags.length > 0" class="tags-list">
            <span v-for="tag in user.subscribedTags" :key="tag.id || tag" class="tag-item">
              {{ tag.name || tag }}
            </span>
          </div>
          <div v-else class="empty-list">No subscribed tags</div>
        </div>

        <div class="info-section">
          <h3 class="section-title">Publications</h3>
          <div v-if="loadingPublications" class="loading-publications">Loading publications...</div>
          <div v-else-if="publicationsError" class="publications-error">{{ publicationsError }}</div>
          <div v-else-if="publications && publications.length > 0" class="publications-list">
            <div v-for="pub in publications" :key="pub.id" class="publication-item">
              <h4 class="pub-title">{{ pub.title }}</h4>
              <p v-if="pub.authors" class="pub-authors">{{ pub.authors }}</p>
              <p v-if="pub.scientificArea" class="pub-area">Area: {{ pub.scientificArea }}</p>
            </div>
          </div>
          <div v-else class="empty-list">No publications</div>
        </div>

        <div class="actions">
          <button @click="goBack" class="btn-back">Back to Users</button>
          <button @click="goToUserHistory" class="btn-history" :disabled="!user?.username">
            View User History
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '~/stores/user-store'
import { useAuthStore } from '~/stores/auth-store'

const userStore = useUserStore()
const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const user = ref(null)
const publications = ref([])
const loading = ref(true)
const loadingPublications = ref(false)
const error = ref(null)
const publicationsError = ref(null)

useAuthErrorRedirect(error)
useAuthErrorRedirect(publicationsError)

onMounted(async () => {
  if (!authStore.isAuthenticated || authStore.user?.role !== 'ADMINISTRADOR') {
    router.push('/publication/searchPublications')
    return
  }
  const userId = route.query.id
  
  if (!userId) {
    error.value = 'User ID is required'
    loading.value = false
    return
  }

  try {
    const data = await userStore.getById(userId)
    user.value = data
    
    // Fetch publications after user data is loaded
    if (data.username) {
      await loadPublications(data.username)
    }
  } catch (err) {
    error.value = err.message || 'Failed to load user details'
  } finally {
    loading.value = false
  }
})

async function loadPublications(username) {
  loadingPublications.value = true
  publicationsError.value = null
  
  try {
    const data = await userStore.getPublicationsByUsername(username)
    publications.value = Array.isArray(data) ? data : []
  } catch (err) {
    publicationsError.value = err.message || 'Failed to load publications'
    publications.value = []
  } finally {
    loadingPublications.value = false
  }
}

function goBack() {
  router.push('/users/users')
}

function goToUserHistory() {
  if (!user.value?.username) return
  router.push({
    path: '/history/userHistory',
    query: { username: user.value.username }
  })
}
</script>

<style scoped>
.user-details {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

h1 {
  color: #333;
  margin-bottom: 30px;
}

.loading,
.error {
  text-align: center;
  padding: 40px;
  font-size: 16px;
}

.error {
  color: #c33;
  background: #fee;
  border-radius: 4px;
}

.user-card {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
}

.user-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e0e0e0;
}

.user-card h2 {
  color: #0077cc;
  margin: 0;
  font-size: 28px;
}

.status-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-badge.active {
  background: #d4edda;
  color: #155724;
}

.status-badge.blocked {
  background: #f8d7da;
  color: #721c24;
}

.info-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.info-section:last-of-type {
  border-bottom: none;
}

.section-title {
  color: #333;
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 15px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  font-weight: 600;
  color: #666;
  min-width: 80px;
}

.value {
  color: #333;
  font-size: 15px;
}

.role-badge {
  display: inline-block;
  padding: 4px 10px;
  background: #e2e3e5;
  color: #383d41;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  display: inline-block;
  padding: 6px 12px;
  background: #d1ecf1;
  color: #0c5460;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
}

.empty-list {
  color: #666;
  font-style: italic;
  padding: 10px 0;
}

.loading-publications {
  color: #666;
  padding: 10px 0;
  font-size: 14px;
}

.publications-error {
  color: #c33;
  padding: 10px 0;
  font-size: 14px;
}

.publications-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.publication-item {
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 3px solid #0077cc;
}

.pub-title {
  margin: 0 0 8px 0;
  color: #0077cc;
  font-size: 16px;
  font-weight: 600;
}

.pub-authors {
  margin: 0 0 5px 0;
  color: #666;
  font-size: 14px;
  font-style: italic;
}

.pub-area {
  margin: 0;
  color: #666;
  font-size: 13px;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.btn-back {
  padding: 10px 20px;
  background: #0077cc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: background 0.2s;
}

.btn-back:hover {
  background: #005fa3;
}

.btn-history {
  padding: 10px 20px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: background 0.2s, opacity 0.2s;
}

.btn-history:hover {
  background: #5a6268;
}

.btn-history:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
