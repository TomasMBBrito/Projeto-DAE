<template>
  <div class="tags-page">
    <div class="page-header">
      <h1>Tags</h1>
      <button @click="goBack" class="btn-back">← Back to Publications</button>
    </div>

    <!-- Error messages -->
    <div v-if="error" class="error">
      {{ error }}
      <button v-if="isAuthError" @click="goToLogin">Go to Login</button>
      <button v-else @click="retry">Retry</button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading">Loading...</div>

    <!-- Create Tag (only for admins/responsavel) -->
    <div v-if="canEdit" class="create-tag">
      <button v-if="!creatingTag" class="btn-create" @click="creatingTag = true">
        + Create Tag
      </button>

      <div v-else class="create-tag-input">
        <input
          v-model="newTagName"
          placeholder="Enter tag name"
          @keyup.enter="createTag"
          @keyup.esc="() => { creatingTag = false; newTagName = '' }"
        />
        <button class="btn-save" @click="createTag">Save</button>
        <button class="btn-cancel" @click="() => { creatingTag = false; newTagName = '' }">Cancel</button>
      </div>
    </div>

    <!-- Tag List -->
    <div v-if="tags.length > 0" class="tags-list">
      <div v-for="tag in tags" :key="tag.id" class="tag-card">
        <div class="tag-info">
          <div>
            <span v-if="editingTagId !== tag.id" class="tag-name">
              {{ tag.name }}
              <span v-if="tag.visible === false" class="tag-hidden-badge">Hidden</span>
            </span>
            <input
              v-else
              v-model="editingTagName"
              class="tag-input"
              @keyup.enter="saveEdit(tag.id)"
              @keyup.esc="cancelEdit"
            />
          </div>
          
          <div v-if="canEdit && editingTagId !== tag.id" class="tag-stats">
            <span class="tag-stat">{{ tag.publicationCount || 0 }} publications</span>
            <span class="tag-separator">•</span>
            <span class="tag-stat">{{ tag.subscriberCount || 0 }} subscribers</span>
          </div>
        </div>
        <div class="tag-actions">
          <!-- Edit buttons (only for responsavel/administrador) -->
          <template v-if="canEdit">
            <button
            v-if="editingTagId !== tag.id"
            class="btn-details"
            @click="viewDetails(tag.id)"
          >
            View Details
          </button>
            <button
              v-if="editingTagId !== tag.id"
              class="btn-edit"
              :disabled="actionLoading[tag.id]"
              @click="startEdit(tag.id, tag.name)"
            >
              Edit
            </button>

            <template v-else>
              <button class="btn-save" :disabled="actionLoading[tag.id]" @click="saveEdit(tag.id)">
                {{ actionLoading[tag.id] ? 'Saving...' : 'Save' }}
              </button>
              <button class="btn-cancel" :disabled="actionLoading[tag.id]" @click="cancelEdit">
                Cancel
              </button>
            </template>
          </template>

          <!-- Subscribe/Unsubscribe buttons -->
          <button
            v-if="!isSubscribed(tag.id) && editingTagId !== tag.id"
            class="btn-subscribe"
            :disabled="actionLoading[tag.id]"
            @click="handleSubscribe(tag.id)"
          >
            {{ actionLoading[tag.id] ? 'Loading...' : 'Subscribe' }}
          </button>
          <button
            v-else-if="editingTagId !== tag.id"
            class="btn-unsubscribe"
            :disabled="actionLoading[tag.id]"
            @click="handleUnsubscribe(tag.id)"
          >
            {{ actionLoading[tag.id] ? 'Loading...' : 'Unsubscribe' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else-if="!loading" class="empty">
      No tags available
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useTagStore } from '~/stores/tag-store'
import { useAuthStore } from '~/stores/auth-store'
import { useRouter } from 'vue-router'

const tagStore = useTagStore()
const authStore = useAuthStore()
const router = useRouter()

const tags = ref([])
const subscribedTags = ref([])
const loading = ref(false)
const error = ref(null)
const isAuthError = ref(false)
const actionLoading = reactive({})

// Edit state
const editingTagId = ref(null)
const editingTagName = ref('')

// Create tag state
const creatingTag = ref(false)
const newTagName = ref('')

// Check if user can edit (responsavel or administrador)
const canEdit = computed(() => {
  const role = authStore.user?.role?.toLowerCase()
  return role === 'responsavel' || role === 'administrador'
})

onMounted(async () => {
  await loadData()
})

async function loadData() {
  loading.value = true
  error.value = null
  isAuthError.value = false

  try {
    await Promise.all([loadTags(), loadSubscribed()])
  } catch (e) {
    console.error('Failed to load data:', e)
    if (e.message?.includes('Not authenticated') || e.statusCode === 401) {
      error.value = 'You are not authenticated. Please log in.'
      isAuthError.value = true
    } else {
      error.value = e.message || 'Failed to load tags'
    }
  } finally {
    loading.value = false
  }
}

async function loadTags() {
  tags.value = await tagStore.getAll()
  console.log(tags.value)
}

async function loadSubscribed() {
  subscribedTags.value = await tagStore.getSubscribed()
}

function isSubscribed(tagId) {
  return subscribedTags.value.some(t => t.id === tagId)
}

async function handleSubscribe(tagId) {
  actionLoading[tagId] = true
  try {
    await tagStore.subscribe(tagId)
    await loadSubscribed()
  } catch (e) {
    console.error('Subscribe failed:', e)
    error.value = 'Failed to subscribe to tag'
  } finally {
    actionLoading[tagId] = false
  }
}

async function handleUnsubscribe(tagId) {
  actionLoading[tagId] = true
  try {
    await tagStore.unsubscribe(tagId)
    await loadSubscribed()
  } catch (e) {
    console.error('Unsubscribe failed:', e)
    error.value = 'Failed to unsubscribe from tag'
  } finally {
    actionLoading[tagId] = false
  }
}

function startEdit(tagId, currentName) {
  editingTagId.value = tagId
  editingTagName.value = currentName
}

function cancelEdit() {
  editingTagId.value = null
  editingTagName.value = ''
}

// ---------------- Create Tag ----------------
async function createTag() {
  if (!newTagName.value.trim()) {
    error.value = 'Tag name cannot be empty'
    return
  }

  loading.value = true
  error.value = null

  try {
    await tagStore.create(newTagName.value.trim())
    newTagName.value = ''
    creatingTag.value = false
    await loadTags() // reload tags to include the new one
  } catch (e) {
    console.error('Create tag failed:', e)
    if (e?.message?.includes('409')) {
      error.value = 'A tag with this name already exists'
    } else {
      error.value = 'Failed to create tag: ' + (e.message || 'Unknown error')
    }
  } finally {
    loading.value = false
  }
}

// ---------------- Edit Tag ----------------
async function saveEdit(tagId) {
  if (!editingTagName.value.trim()) {
    error.value = 'Tag name cannot be empty'
    return
  }

  actionLoading[tagId] = true
  error.value = null

  try {
    await tagStore.update(tagId, editingTagName.value.trim())
    await loadTags()
    cancelEdit()
  } catch (e) {
    console.error('Update failed:', e)
    error.value = 'Failed to update tag: ' + (e.message || 'Unknown error')
  } finally {
    actionLoading[tagId] = false
  }
}

// Retry / Login
async function retry() {
  await loadData()
}

function viewDetails(tagId) {
  router.push(`/tags/${tagId}`)
}

function goToLogin() {
  router.push('/auth/login')
}

function goBack() {
  router.push('/publication/searchPublications')
}
</script>

<style scoped>
/* Global font */
.tags-page {
  font-family: "Inter", sans-serif;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* Page Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

h1 {
  color: #333;
  margin: 0;
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

.loading,
.empty {
  text-align: center;
  padding: 40px;
  color: #666;
  font-size: 16px;
}

.error {
  color: #c33;
  background: #fee;
  border-radius: 4px;
  padding: 20px;
  text-align: center;
  margin-bottom: 20px;
}

.error button {
  margin-top: 10px;
  padding: 8px 16px;
  background: #0077cc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.error button:hover {
  background: #005fa3;
}

/* Create Tag Section */
.create-tag {
  margin-bottom: 30px;
}

.btn-create {
  padding: 10px 20px;
  background: white;
  color: #0077cc;
  border: 2px solid #0077cc;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
}

.btn-create:hover {
  background: #0077cc;
  color: white;
}

.create-tag-input {
  display: flex;
  gap: 10px;
  align-items: center;
}

.create-tag-input input {
  flex: 1;
  max-width: 400px;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.create-tag-input input:focus {
  outline: none;
  border-color: #0077cc;
}

/* Tag List */
.tags-list {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: grid;
  gap: 15px;
}

.tag-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
  border-left: 3px solid #0077cc;
  transition: background 0.2s;
}

.tag-card:hover {
  background: #f0f0f0;
}

.tag-info {
  flex: 1;
  margin-right: 15px;
}

.tag-name {
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.tag-input {
  padding: 8px 12px;
  border: 2px solid #0077cc;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 600;
  width: 100%;
  max-width: 300px;
}

.tag-input:focus {
  outline: none;
  border-color: #005fa3;
}

/* Tag Actions Buttons */
.tag-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.btn-subscribe,
.btn-unsubscribe,
.btn-edit,
.btn-details,
.btn-save,
.btn-cancel {
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  transition: all 0.2s;
}

.btn-edit {
  background: #0077cc;
  color: white;
  border: 2px solid #0077cc;
}

.btn-edit:hover:not(:disabled) {
  background: #005fa3;
  border-color: #005fa3;
}

.btn-save {
  background: #0077cc;
  color: white;
  border: 2px solid #0077cc;
}

.btn-save:hover:not(:disabled) {
  background: #005fa3;
  border-color: #005fa3;
}

.btn-cancel {
  background: #f0f0f0;
  color: #333;
  border: 2px solid #ddd;
}

.btn-cancel:hover:not(:disabled) {
  background: #e0e0e0;
  border-color: #ccc;
}

.btn-subscribe {
  background: white;
  color: #0077cc;
  border: 2px solid #0077cc;
  min-width: 100px;
}

.btn-subscribe:hover:not(:disabled) {
  background: #0077cc;
  color: white;
}

.btn-unsubscribe {
  background: #ff6b6b;
  color: white;
  border: 2px solid #ff6b6b;
  min-width: 100px;
}

.btn-unsubscribe:hover:not(:disabled) {
  background: #ee5a52;
  border-color: #ee5a52;
}

/* Disabled state */
.btn-subscribe:disabled,
.btn-unsubscribe:disabled,
.btn-edit:disabled,
.btn-save:disabled,
.btn-cancel:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.tag-hidden-badge {
  display: inline-block;
  margin-left: 10px;
  padding: 2px 8px;
  background: #ff6b6b;
  color: white;
  font-size: 11px;
  font-weight: 600;
  border-radius: 3px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.tag-stats {
  display: flex;
  gap: 15px;
  margin-top: 6px;
  font-size: 13px;
  color: #666;
}

.tag-stat {
  color: #666;
}

.tag-separator {
  color: #ccc;
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