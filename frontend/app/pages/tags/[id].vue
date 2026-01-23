<!-- pages/tags/[id].vue -->
<template>
  <div class="tag-details">
    <button @click="goBack" class="btn-back">← Back to Tags</button>

    <div v-if="loading" class="loading">Loading...</div>

    <div v-else-if="error" class="error">{{ error }}</div>

    <div v-else-if="tag" class="tag-content">
      <!-- Tag Header -->
      <div class="tag-header">
        <div class="tag-title-section">
          <h1>
            {{ tag.name }}
            <span v-if="tag.visible === false" class="hidden-badge">Hidden</span>
          </h1>
          <div class="tag-meta">
            <span>{{ publications.length }} publications</span>
            <span class="separator">•</span>
            <span>{{ subscribers.length }} subscribers</span>
          </div>
        </div>
      </div>

      <!-- Publications Section -->
      <div class="section">
        <h2>Publications ({{ publications.length }})</h2>
        
        <div v-if="publicationsLoading" class="loading-inline">Loading publications...</div>
        
        <div v-else-if="publications.length === 0" class="empty-section">
          No publications associated with this tag yet.
        </div>
        
        <div v-else class="publications-list">
          <div
            v-for="pub in publications"
            :key="pub.id"
            class="publication-card"
            @click="viewPublication(pub.id)"
          >
            <div class="publication-info">
              <h3>{{ pub.title }}</h3>
              <p class="publication-meta">
                <span>{{ formatDate(pub.publicationDate) }}</span>
                <span class="separator">•</span>
                <span>{{ pub.scientificArea }}</span>
              </p>
              <p v-if="pub.summary" class="publication-summary">{{ truncate(pub.summary, 150) }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Subscribers Section (only for admins/responsavel) -->
      <div v-if="canEdit" class="section">
        <h2>Subscribers ({{ subscribers.length }})</h2>
        
        <div v-if="subscribersLoading" class="loading-inline">Loading subscribers...</div>
        
        <div v-else-if="subscribers.length === 0" class="empty-section">
          No subscribers yet.
        </div>
        
        <div v-else class="subscribers-list">
          <div
            v-for="user in subscribers"
            :key="user.username"
            class="subscriber-card"
          >
            <div class="subscriber-info">
              <span class="subscriber-name">{{ user.name }}</span>
              <span class="subscriber-username">@{{ user.username }}</span>
            </div>
            <span class="subscriber-role">{{ user.role }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useTagStore } from '~/stores/tag-store'
import { useAuthStore } from '~/stores/auth-store'
import { useRouter, useRoute } from 'vue-router'

const tagStore = useTagStore()
const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const tag = ref(null)
const publications = ref([])
const subscribers = ref([])
const loading = ref(false)
const publicationsLoading = ref(false)
const subscribersLoading = ref(false)
const error = ref('')
const actionLoading = ref(false)
const showDeleteModal = ref(false)

const tagId = computed(() => parseInt(route.params.id))

const canEdit = computed(() => {
  const role = authStore.user?.role?.toLowerCase()
  return role === 'responsavel' || role === 'administrador'
})

onMounted(() => {
  loadTagDetails()
  loadPublications()
  if (canEdit.value) {
    loadSubscribers()
  }
})

async function loadTagDetails() {
  loading.value = true
  error.value = ''
  
  try {
    tag.value = await tagStore.getById(tagId.value)
  } catch (e) {
    error.value = e.message || 'Failed to load tag details'
  } finally {
    loading.value = false
  }
}

async function loadPublications() {
  publicationsLoading.value = true
  
  try {
    publications.value = await tagStore.getPublications(tagId.value)
  } catch (e) {
    console.error('Failed to load publications:', e)
  } finally {
    publicationsLoading.value = false
  }
}

async function loadSubscribers() {
  subscribersLoading.value = true
  
  try {
    subscribers.value = await tagStore.getSubscribers(tagId.value)
  } catch (e) {
    console.error('Failed to load subscribers:', e)
  } finally {
    subscribersLoading.value = false
  }
}

function viewPublication(pubId) {
  router.push(`/publication/${pubId}`)
}

function goBack() {
  router.push('/tags')
}

function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString()
}

function truncate(text, length) {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}
</script>

<style scoped>
.tag-details {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: "Inter", sans-serif;
}

.btn-back {
  background: #2755d5;
  border: none;
  padding: 10px 15px;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 20px;
  font-size: 14px;
  font-weight: 600;
  color: white;
}

.btn-back:hover {
  background: #1640b3;
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

.tag-content {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.tag-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 30px;
  border-bottom: 2px solid #eee;
  margin-bottom: 30px;
}

.tag-title-section h1 {
  margin: 0 0 10px 0;
  color: #333;
  display: flex;
  align-items: center;
  gap: 12px;
}

.hidden-badge {
  background: #ff6b6b;
  color: white;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 4px;
  text-transform: uppercase;
}

.tag-meta {
  display: flex;
  gap: 10px;
  color: #666;
  font-size: 14px;
}

.separator {
  color: #ccc;
}

.tag-header-actions {
  display: flex;
  gap: 10px;
}

.btn-toggle,
.btn-delete {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-toggle {
  background: #f0f0f0;
  color: #333;
}

.btn-toggle:hover:not(:disabled) {
  background: #0077cc;
  color: white;
}

.btn-delete {
  background: #ff6b6b;
  color: white;
}

.btn-delete:hover:not(:disabled) {
  background: #ee5a52;
}

.btn-toggle:disabled,
.btn-delete:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.section {
  margin-bottom: 40px;
}

.section:last-child {
  margin-bottom: 0;
}

.section h2 {
  color: #333;
  margin: 0 0 20px 0;
  font-size: 20px;
}

.loading-inline {
  text-align: center;
  padding: 20px;
  color: #666;
  font-style: italic;
}

.empty-section {
  text-align: center;
  padding: 40px;
  color: #999;
  background: #f9f9f9;
  border-radius: 4px;
  font-style: italic;
}

.publications-list {
  display: grid;
  gap: 15px;
}

.publication-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 4px;
  border-left: 3px solid #0077cc;
  cursor: pointer;
  transition: all 0.2s;
}

.publication-card:hover {
  background: #f0f0f0;
  transform: translateX(4px);
}

.publication-info {
  flex: 1;
  margin-right: 20px;
}

.publication-info h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 18px;
}

.publication-meta {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 13px;
}

.publication-summary {
  margin: 0;
  color: #666;
  line-height: 1.5;
  font-size: 14px;
}

.publication-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-end;
  font-size: 14px;
  color: #666;
}

.subscribers-list {
  display: grid;
  gap: 12px;
}

.subscriber-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
  border-left: 3px solid #0077cc;
}

.subscriber-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.subscriber-name {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.subscriber-username {
  color: #666;
  font-size: 13px;
}

.subscriber-role {
  background: #0077cc;
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 8px;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.modal-content h2 {
  margin: 0 0 15px 0;
  color: #333;
}

.modal-content p {
  margin: 10px 0;
  color: #666;
}

.warning {
  color: #ff6b6b;
  font-weight: 600;
}

.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 25px;
}

.btn-cancel {
  padding: 8px 16px;
  background: #f0f0f0;
  color: #333;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-delete-confirm {
  padding: 8px 16px;
  background: #ff6b6b;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.btn-delete-confirm:hover:not(:disabled) {
  background: #ee5a52;
}

.btn-delete-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-details {
  background: #f0f0f0;
  color: #333;
  border: 2px solid #ddd;
}

.btn-details:hover {
  background: #e0e0e0;
  border-color: #ccc;
}
</style>