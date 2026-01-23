<!-- pages/publication/details/[id].vue -->
<template>
  <div class="edit-publication">
    <nav class="navbar">
      <div class="navbar-brand">
        <h1>Edit Publication</h1>
      </div>
      <div class="navbar-actions">
        <button @click="goBack" class="btn-nav">Cancel</button>
        <button @click="goToProfile" class="btn-nav">Profile</button>
      </div>
    </nav>

    <div v-if="loading" class="loading">Loading publication...</div>

    <div v-else-if="loadError" class="error">
      {{ loadError }}
      <button @click="goBack" class="btn-back-error">Go Back</button>
    </div>

    <div v-else class="form-container">
      <form @submit.prevent="handleSubmit" class="pub-form">
        <div class="form-row">
          <label>Title *</label>
          <input v-model="form.title" type="text" required />
        </div>

        <div class="form-row">
          <label>Summary *</label>
          <textarea v-model="form.summary" rows="4" required></textarea>
        </div>

        <div class="form-row">
          <label>Scientific Area *</label>
          <select v-model="form.scientificArea" required>
            <option disabled value="">Select area</option>
            <option value="COMPUTER_SCIENCE">COMPUTER_SCIENCE</option>
            <option value="BIOLOGY">BIOLOGY</option>
            <option value="CHEMISTRY">CHEMISTRY</option>
            <option value="PHYSICS">PHYSICS</option>
            <option value="MATHEMATICS">MATHEMATICS</option>
            <option value="ENGINEERING">ENGINEERING</option>
            <option value="ENVIRONMENTAL_SCIENCE">ENVIRONMENTAL_SCIENCE</option>
            <option value="SOCIAL_SCIENCES">SOCIAL_SCIENCES</option>
            <option value="HUMANITIES">HUMANITIES</option>
          </select>
        </div>

        <div class="form-row">
          <label>Publication Date *</label>
          <input v-model="form.publicationDate" type="date" required />
        </div>

        <div class="form-row">
          <label>Authors (comma separated)</label>
          <input v-model="form.authors" placeholder="Alice,Bob,Charlie" />
        </div>

        <div class="form-row">
          <label>Tags</label>
          <div v-if="loadingTags" class="loading-tags">Loading tags...</div>
          <div v-else class="tags-section">
            <div class="tags-list">
              <button
                  v-for="tag in availableTags"
                  :key="tag.id"
                  type="button"
                  @click="toggleTag(tag.id)"
                  :class="['tag-chip', { active: selectedTags.includes(tag.id) }]"
              >
                {{ tag.name }}
                <span v-if="selectedTags.includes(tag.id)" class="check-icon">✓</span>
              </button>
            </div>
            <p v-if="availableTags.length === 0" class="no-tags">No tags available</p>
          </div>
        </div>

        <div class="form-actions">
          <button type="submit" :disabled="submitting" class="btn-submit">
            {{ submitting ? 'Saving...' : 'Save Changes' }}
          </button>
          <button type="button" @click="goBack" class="btn-cancel">Cancel</button>
        </div>

        <div v-if="error" class="error">{{ error }}</div>
        <div v-if="success" class="success">{{ success }}</div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { usePublicationStore } from '~/stores/publication-store'
import { useTagStore } from '~/stores/tag-store'
import { useAuthStore } from '~/stores/auth-store'

const publicationStore = usePublicationStore()
const tagStore = useTagStore()
const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const publicationId = computed(() => parseInt(route.params.id))

const form = ref({
  title: '',
  summary: '',
  scientificArea: '',
  publicationDate: '',
  authors: ''
})

const originalPublication = ref(null)
const availableTags = ref([])
const selectedTags = ref([])
const originalTagIds = ref([])

const loading = ref(false)
const loadingTags = ref(false)
const submitting = ref(false)
const loadError = ref('')
const error = ref('')
const success = ref('')

onMounted(async () => {
  await loadPublication()
  await loadTags()
})

async function loadPublication() {
  loading.value = true
  loadError.value = ''

  try {
    const pub = await publicationStore.getById(publicationId.value)
    originalPublication.value = pub

    if (!canEdit(pub)) {
      loadError.value = 'You do not have permission to edit this publication'
      return
    }

    form.value = {
      title: pub.title || '',
      summary: pub.summary || '',
      scientificArea: pub.scientificArea || '',
      publicationDate: pub.publicationDate || '',
      authors: pub.authors && pub.authors.length > 0 ? pub.authors.join(',') : ''
    }

    if (pub.tags && pub.tags.length > 0) {
      originalTagIds.value = pub.tags.map(t => t.id)
      selectedTags.value = [...originalTagIds.value]
    }

  } catch (e) {
    loadError.value = e.message || 'Failed to load publication'
  } finally {
    loading.value = false
  }
}

async function loadTags() {
  loadingTags.value = true
  try {
    availableTags.value = await tagStore.getAll()
  } catch (e) {
    console.error('Failed to load tags', e)
  } finally {
    loadingTags.value = false
  }
}

function canEdit(pub) {
  if (!pub || !authStore.user) return false

  const role = authStore.user.role
  const isAuthor = pub.submitterUsername === authStore.user.username

  return isAuthor || role === 'RESPONSAVEL' || role === 'ADMINISTRADOR'
}

function toggleTag(tagId) {
  const idx = selectedTags.value.indexOf(tagId)
  if (idx > -1) {
    selectedTags.value.splice(idx, 1)
  } else {
    selectedTags.value.push(tagId)
  }
}

async function handleSubmit() {
  error.value = ''
  success.value = ''

  if (!form.value.title || !form.value.summary || !form.value.scientificArea || !form.value.publicationDate) {
    error.value = 'All required fields must be filled'
    return
  }

  submitting.value = true

  try {
    const authors = form.value.authors && form.value.authors.trim() !== ''
        ? form.value.authors.split(',').map(a => a.trim()).filter(a => a !== '')
        : []

    await publicationStore.update(publicationId.value, {
      title: form.value.title,
      summary: form.value.summary,
      scientificArea: form.value.scientificArea,
      publicationDate: form.value.publicationDate,
      authors: authors
    })

    await updateTags()

    success.value = 'Publication updated successfully!'

    setTimeout(() => {
      router.push(`/publication/${publicationId.value}`)
    }, 1500)

  } catch (e) {
    console.error(e)
    error.value = e?.message || 'Failed to update publication'
  } finally {
    submitting.value = false
  }
}

async function updateTags() {
  const tagsToAdd = selectedTags.value.filter(id => !originalTagIds.value.includes(id))

  const tagsToRemove = originalTagIds.value.filter(id => !selectedTags.value.includes(id))

  for (const tagId of tagsToAdd) {
    try {
      await publicationStore.addTag(publicationId.value, tagId)
    } catch (e) {
      console.error(`Failed to add tag ${tagId}:`, e)
    }
  }

  if (authStore.user?.role === 'RESPONSAVEL' || authStore.user?.role === 'ADMINISTRADOR') {
    for (const tagId of tagsToRemove) {
      try {
        await publicationStore.removeTag(publicationId.value, tagId)
      } catch (e) {
        console.error(`Failed to remove tag ${tagId}:`, e)
      }
    }
  }
}

function goBack() {
  router.push(`/publication/${publicationId.value}`)
}

function goToProfile() {
  const username = authStore.user?.username || authStore.username
  if (username) router.push(`/users/${username}`)
}
</script>

<style scoped>
.edit-publication {
  font-family: "Inter", sans-serif;
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  margin-bottom: 20px;
  border-bottom: 2px solid #e0e0e0;
}

.navbar-brand h1 {
  margin: 0;
  color: #333;
  font-size: 22px;
}

.navbar-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.btn-nav {
  background: transparent;
  color: #0077cc;
  border: 1px solid #0077cc;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.btn-nav:hover {
  background: #0077cc;
  color: white;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
  font-size: 16px;
}

.error {
  margin-top: 10px;
  background: #fee;
  color: #c33;
  padding: 12px;
  border-radius: 6px;
  text-align: center;
}

.btn-back-error {
  margin-top: 10px;
  padding: 8px 16px;
  background: #0077cc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.btn-back-error:hover {
  background: #005fa3;
}

.form-container {
  background: white;
  padding: 18px;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

.pub-form {
  display: grid;
  gap: 12px;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-row label {
  font-size: 14px;
  color: #333;
  font-weight: 600;
}

.form-row input[type="text"],
.form-row input[type="date"],
.form-row textarea,
.form-row select {
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
}

.form-row textarea {
  resize: vertical;
  min-height: 100px;
}

.loading-tags {
  color: #666;
  font-size: 14px;
  font-style: italic;
}

.tags-section {
  padding: 10px 0;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-chip {
  padding: 6px 12px;
  border-radius: 18px;
  border: 1px solid #ddd;
  background: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  transition: all 0.2s;
}

.tag-chip:hover {
  border-color: #0077cc;
  background: #f0f8ff;
}

.tag-chip.active {
  background: #0077cc;
  color: white;
  border-color: #0077cc;
}

.check-icon {
  font-weight: bold;
}

.no-tags {
  color: #999;
  font-style: italic;
  font-size: 14px;
  margin: 0;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 6px;
}

.btn-submit {
  padding: 10px 18px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 700;
}

.btn-submit:hover:not(:disabled) {
  background: #218838;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-cancel {
  padding: 10px 16px;
  background: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
}

.btn-cancel:hover {
  background: #e2e6ea;
}

.success {
  margin-top: 10px;
  background: #eef9f0;
  color: #2d7a2d;
  padding: 12px;
  border-radius: 6px;
  text-align: center;
}
</style>