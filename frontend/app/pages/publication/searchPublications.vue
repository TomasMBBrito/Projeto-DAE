<!-- pages/publication/searchPublications.vue -->
<template>
  <div class="search-publications">
    <h1>Search Publications</h1>

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

        <div class="tags-button">
          <button @click="goToTags" class="btn-tags">Go to Tags</button>
        </div>
      </div>
    </div>

    <div v-if="loading" class="loading">Loading...</div>

    <div v-else-if="error" class="error">{{ error }}</div>

    <div v-else-if="publications.length === 0" class="empty">
      <p>No publications found</p>
    </div>

    <div v-else class="publications-list">
      <div v-for="pub in publications" :key="pub.id" class="publication-card" @click="goToDetails(pub.id)">
        <h3>{{ pub.title }}</h3>
        <p class="authors">{{ pub.authors }}</p>
        <p class="summary">{{ truncate(pub.summary, 150) }}</p>

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
</template>

<script setup>
import { usePublicationStore } from '~/stores/publication-store';

const publicationStore = usePublicationStore()
const router = useRouter()

const searchQuery = ref('')
const sortBy = ref('')
const publications = ref([])
const loading = ref(false)
const error = ref('')

onMounted(() => {
  loadPublications()
})

function goToTags() {
  router.push('/tags')
}


async function loadPublications() {
  loading.value = true
  error.value = ''

  try {
    publications.value = await publicationStore.getAll(sortBy.value)
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
</script>

<style scoped>
.search-publications {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  color: #333;
  margin-bottom: 30px;
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
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.publication-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.publication-card h3 {
  margin: 0 0 10px 0;
  color: #0077cc;
  font-size: 18px;
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
</style>