<template>
  <div class="tags-page">
    <h1>Tags</h1>

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
          <span v-if="editingTagId !== tag.id" class="tag-name">{{ tag.name }}</span>
          <input
            v-else
            v-model="editingTagName"
            class="tag-input"
            @keyup.enter="saveEdit(tag.id)"
            @keyup.esc="cancelEdit"
          />
        </div>

        <div class="tag-actions">
          <!-- Edit buttons (only for responsavel/administrador) -->
          <template v-if="canEdit">
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

function goToLogin() {
  router.push('/auth/login')
}
</script>

<style scoped>
.tags-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  margin-bottom: 24px;
}

.loading,
.empty,
.error {
  text-align: center;
  padding: 40px;
  color: #666;
}

.error {
  color: #dc3545;
  background: #fee;
  border-radius: 6px;
  margin-bottom: 20px;
}

.error button {
  margin-top: 10px;
  padding: 8px 16px;
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.error button:hover {
  background: #c82333;
}

.tags-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tag-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px;
  background: white;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.tag-info {
  flex: 1;
  margin-right: 12px;
}

.tag-name {
  font-weight: 600;
  font-size: 16px;
}

.tag-input {
  padding: 6px 10px;
  border: 2px solid #007bff;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 600;
  width: 100%;
  max-width: 300px;
}

.tag-input:focus {
  outline: none;
  border-color: #0056b3;
}

.tag-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.btn-subscribe,
.btn-unsubscribe,
.btn-edit,
.btn-save,
.btn-cancel {
  border: none;
  padding: 6px 14px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  white-space: nowrap;
}

.btn-subscribe {
  background: #28a745;
  color: white;
  min-width: 100px;
}

.btn-subscribe:hover:not(:disabled) {
  background: #218838;
}

.btn-unsubscribe {
  background: #dc3545;
  color: white;
  min-width: 100px;
}

.btn-unsubscribe:hover:not(:disabled) {
  background: #c82333;
}

.btn-edit {
  background: #007bff;
  color: white;
}

.btn-edit:hover:not(:disabled) {
  background: #0056b3;
}

.btn-save {
  background: #28a745;
  color: white;
}

.btn-save:hover:not(:disabled) {
  background: #218838;
}

.btn-cancel {
  background: #6c757d;
  color: white;
}

.btn-cancel:hover:not(:disabled) {
  background: #5a6268;
}

.btn-subscribe:disabled,
.btn-unsubscribe:disabled,
.btn-edit:disabled,
.btn-save:disabled,
.btn-cancel:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.create-tag {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.create-tag input {
  padding: 6px 10px;
  border: 2px solid #007bff;
  border-radius: 4px;
  font-size: 14px;
}

.btn-create {
  background: #007bff;
  color: white;
  padding: 6px 14px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
}

.btn-create:hover:not(:disabled) {
  background: #0056b3;
}
</style>
