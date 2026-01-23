<!-- pages/publication/searchPublications.vue -->
<template>
  <div class="search-publications">
    <!-- Navbar -->
    <nav class="navbar">
      <div class="navbar-brand">
        <h1>Search Publications</h1>
        <button class="btn-create" @click="goToCreate">Create Publication</button>
      </div>
      <div class="navbar-actions">
        <button @click="goToEmails" class="btn-nav">Emails</button>
        <button @click="goToProfile" class="btn-nav">Profile</button>
        <button @click="goToTags" class="btn-nav">Tags</button>
        <button @click="goToUsers" class="btn-nav">Users</button>
        <button @click="goLogout" class="btn-logout">Logout</button>
      </div>
    </nav>

    <div class="search-controls">
      <div class="search-bar">
        <input v-model="searchQuery" type="text" placeholder="Search by ID, title, author..." class="search-input"
               @keyup.enter="searchPublications" />
        <button @click="searchPublications" class="btn-search">Search</button>
      </div>

      <div class="sort-controls">
        <label>Sort by:</label>
        <select v-model="sortBy" @change="loadPublications" class="sort-select">
          <option value="">Default</option>
          <option value="recent">Recent</option>
          <option value="comments">Comments</option>
          <option value="rating">Rating</option>
        </select>
      </div>
    </div>

    <div class="tag-filter-section">
      <div class="tag-filter-header">
        <h3>Filter by Tags</h3>
        <button
            v-if="selectedTags.length > 0"
            @click="clearTagFilter"
            class="btn-clear-filter"
        >
          Clear Filter ({{ selectedTags.length }})
        </button>
      </div>

      <div v-if="loadingTags" class="loading-tags">Loading tags...</div>

      <div v-else-if="availableTags.length === 0" class="no-tags">
        No tags available
      </div>

      <div v-else class="tags-list">
        <button
            v-for="tag in availableTags"
            :key="tag.id"
            @click="toggleTag(tag.id)"
            class="tag-chip"
            :class="{ active: selectedTags.includes(tag.id) }"
        >
          {{ tag.name }}
          <span v-if="selectedTags.includes(tag.id)" class="check-icon">✓</span>
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading">Loading...</div>

    <div v-else-if="error" class="error">{{ error }}</div>

    <div v-else-if="publications.length === 0" class="empty">
      <p>No publications found</p>
    </div>

    <div v-else class="publications-list">
      <div v-for="pub in publications" :key="pub.id" class="publication-card">
        <div class="card-header">
          <h3 @click="goToDetails(pub.id)">{{ pub.title }}</h3>

          <button
              v-if="canEdit(pub)"
              @click.stop="toggleVisibility(pub)"
              class="btn-visibility-icon"
              :title="pub.visible ? 'Hide publication' : 'Show publication'"
          >
            {{ pub.visible ? 'Hide' : 'Show' }}
          </button>
        </div>

        <div v-if="pub.visible === false && canEdit(pub)" class="hidden-badge-card">
          Hidden
        </div>

        <div @click="goToDetails(pub.id)" style="cursor: pointer;">
          <p class="authors">Submitted by: {{ pub.submitterUsername }}</p>
          <p class="summary">{{ truncate(pub.summary, 150) }}</p>

          <span v-if="canEdit(pub)">
            <button @click.stop="goToHistory(pub.id)" class="btn-history">View History</button>
          </span>

          <div class="publication-meta">
            <span class="meta-item">
              Area: {{ pub.scientificArea }}
            </span>
            <span class="meta-item">
              Comments: {{ pub.commentCount || 0 }}
            </span>
            <span class="meta-item">
              Rating: {{ pub.averageRating ? pub.averageRating.toFixed(1) : 'N/A' }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { usePublicationStore } from '~/stores/publication-store';
import { useTagStore } from '~/stores/tag-store';
import { useAuthStore } from '~/stores/auth-store';

const publicationStore = usePublicationStore()
const router = useRouter()
const tagStore = useTagStore()
const authStore = useAuthStore()

const searchQuery = ref('')
const sortBy = ref('')
const publications = ref([])
const loading = ref(false)
const error = ref('')

const availableTags = ref([])
const loadingTags = ref(false)
const selectedTags = ref([])

onMounted(() => {
  loadTags()
  loadPublications()
})

function canEdit(publication){
  const username = authStore.user?.username
  const role = authStore.user?.role?.toLowerCase()
  return role === 'responsavel' || role === 'administrador' || publication.submitterUsername === username
}

async function toggleVisibility(publication) {
  if (!confirm(`Are you sure you want to ${publication.visible ? 'hide' : 'show'} this publication?`)) {
    return
  }

  try {
    const newVisibility = !publication.visible
    await publicationStore.toggleVisibility(publication.id, newVisibility)
    publication.visible = newVisibility
    await loadPublications()
  } catch (e) {
    alert('Failed to toggle visibility: ' + e.message)
  }
}

function goToTags() {
  router.push('/tags')
}

function goToUsers() {
  router.push('/users')
}

function goToHistory(id) {
  router.push(`/publication/${id}-history`)
}

function goToEmails() {
  router.push('/mails')
}

function goToProfile() {
  const username = authStore.user?.username || authStore.username
  if (username) {
    router.push(`/users/${username}`)
  }
}

function goToCreate() {
  router.push('/publication/createPublication')
}

async function loadPublications() {
  loading.value = true
  error.value = ''

  try {
    publications.value = await publicationStore.getAll(sortBy.value)
    console.log(publications.value)
  } catch (e) {
    error.value = e.message || 'Failed to load publications'
  } finally {
    loading.value = false
  }
}

async function searchPublications() {
  if (!searchQuery.value.trim()) {
    loadPublications()
    return
  }

  loading.value = true
  error.value = ''

  try {
    publications.value = await publicationStore.search(searchQuery.value)
  } catch (e) {
    error.value = e.message || 'Search failed'
  } finally {
    loading.value = false
  }
}

function goToDetails(id) {
  router.push(`/publication/${id}`)
}

function truncate(text, length) {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

async function loadTags() {
  loadingTags.value = true
  try {
    availableTags.value = await tagStore.getAll()
  } catch (e) {
    console.error('Failed to load tags:', e)
  } finally {
    loadingTags.value = false
  }
}

async function toggleTag(tagId) {
  const index = selectedTags.value.indexOf(tagId)

  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(tagId)
  }

  searchQuery.value = ''
  await filterBySelectedTags()
}

async function filterBySelectedTags() {
  if (selectedTags.value.length === 0) {
    await loadPublications()
    return
  }

  loading.value = true
  error.value = ''

  try {
    publications.value = await publicationStore.filterByTags(selectedTags.value)
  } catch (e) {
    error.value = e.message || 'Failed to filter publications'
  } finally {
    loading.value = false
  }
}

function clearTagFilter() {
  selectedTags.value = []
  loadPublications()
}

function goLogout() {
  router.push(`/auth/login`)
}
</script>

<style scoped>
.search-publications {
  font-family: "Inter", sans-serif;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  margin-bottom: 30px;
  border-bottom: 2px solid #e0e0e0;
}

.navbar-brand h1 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.navbar-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.btn-nav {
  background: transparent;
  color: #0077cc;
  border: 1px solid #0077cc;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-nav:hover {
  background: #0077cc;
  color: white;
}

.btn-logout {
  background: #dc3545;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s;
}

.btn-logout:hover {
  background: #c82333;
}

.search-publications {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.search-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.search-bar {
  display: flex;
  gap: 10px;
  flex: 1;
  min-width: 300px;
}

.search-input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.search-input:focus {
  outline: none;
  border-color: #0077cc;
}

.btn-search {
  padding: 10px 20px;
  background: #0077cc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.btn-search:hover {
  background: #005fa3;
}

.sort-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sort-controls label {
  font-size: 14px;
  color: #666;
}

.sort-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.tag-filter-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.tag-filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.tag-filter-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.btn-clear-filter {
  padding: 6px 12px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}

.btn-clear-filter:hover {
  background: #5a6268;
}

.loading-tags,
.no-tags {
  color: #666;
  font-size: 14px;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-chip {
  padding: 6px 12px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 5px;
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

.publications-list {
  display: grid;
  gap: 20px;
}

.publication-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.publication-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 15px;
  margin-bottom: 10px;
}

.card-header h3 {
  margin: 0;
  color: #0077cc;
  font-size: 18px;
  flex: 1;
  cursor: pointer;
}

.card-header h3:hover {
  text-decoration: underline;
}

.btn-visibility-icon {
  background: transparent;
  border: 2px solid #ddd;
  padding: 8px 12px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.2s;
  flex-shrink: 0;
}

.btn-visibility-icon:hover {
  background: #f0f0f0;
  border-color: #ffc107;
  transform: scale(1.1);
}

.hidden-badge-card {
  background: #fff3cd;
  border: 1px solid #ffc107;
  color: #856404;
  padding: 6px 12px;
  border-radius: 4px;
  margin-bottom: 10px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
}

.authors {
  color: #666;
  font-size: 14px;
  margin: 0 0 10px 0;
  font-style: italic;
}

.summary {
  color: #333;
  line-height: 1.5;
  margin: 0 0 15px 0;
  font-size: 14px;
}

.publication-meta {
  display: flex;
  gap: 20px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.meta-item {
  font-size: 13px;
  color: #666;
}

.btn-create {
  padding: 12px 24px;
  margin-top: 10px;
  background: #1453d1;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 700;
  box-shadow: 0 2px 6px rgba(40,167,69,0.2);
}

.btn-create:hover {
  background: #0a3fab;
}

.btn-history {
  padding: 8px 16px;
  background: #1453d1;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  margin-bottom: 15px;
}

.btn-history:hover {
  background: #0a3fab;
}
</style>