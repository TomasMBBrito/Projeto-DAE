<template>
  <div class="tags-page">
    <h1>Tags Management</h1>

    <!-- Create new tag -->
    <div class="new-tag">
      <input v-model="newTagName" placeholder="New tag name" />
      <button @click="createTag">Add Tag</button>
    </div>

    <div v-if="loading" class="loading">Loading...</div>
    <div v-if="error" class="error">{{ error }}</div>

    <div v-if="!loading && tags.length === 0" class="empty">No tags found</div>

    <div class="tags-list">
      <div v-for="tag in tags" :key="tag.id" class="tag-card">
        <div class="tag-info">
          <strong>{{ tag.name }}</strong>
          <span>Publications: {{ tag.publicationCount || 0 }}</span>
          <span>Subscribers: {{ tag.subscriberCount || 0 }}</span>
        </div>

        <div class="tag-actions">
          <button @click="toggleSubscribers(tag)">View Subscribers</button>
          <button @click="editTag(tag)">Edit</button>
          <button @click="deleteTag(tag)">Delete</button>
        </div>

        <div v-if="tag.showSubscribers" class="subscribers-list">
          <h4>Subscribers:</h4>
          <ul>
            <li v-for="user in tag.subscribers" :key="user.id">
              {{ user.username }} ({{ user.email }})
            </li>
          </ul>
        </div>

        <div v-if="tag.editing" class="edit-tag">
          <input v-model="tag.newName" />
          <button @click="saveTag(tag)">Save</button>
          <button @click="cancelEdit(tag)">Cancel</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue"
import { useTagStore } from "~/stores/tag-store"

const tagStore = useTagStore()

const tags = ref([])
const newTagName = ref('')
const loading = ref(false)
const error = ref('')

async function loadTags() {
  loading.value = true
  error.value = ''
  try {
    const data = await tagStore.getAll()
    // Add frontend helper fields
    tags.value = data.map(tag => ({
      ...tag,
      showSubscribers: false,
      editing: false,
      newName: tag.name,
      subscribers: []
    }))
  } catch (e) {
    error.value = e.message || 'Failed to load tags'
  } finally {
    loading.value = false
  }
}

async function toggleSubscribers(tag) {
  if (tag.showSubscribers) {
    tag.showSubscribers = false
    return
  }
  try {
    const subs = await tagStore.getSubscribers(tag.id)
    tag.subscribers = subs
    tag.showSubscribers = true
  } catch (e) {
    alert('Failed to load subscribers: ' + e.message)
  }
}

function editTag(tag) {
  tag.editing = true
}

function cancelEdit(tag) {
  tag.editing = false
  tag.newName = tag.name
}

async function saveTag(tag) {
  if (!tag.newName.trim()) {
    alert("Tag name cannot be empty")
    return
  }
  try {
    await tagStore.update(tag.id, tag.newName)
    tag.name = tag.newName
    tag.editing = false
  } catch (e) {
    alert("Failed to update tag: " + e.message)
  }
}

async function deleteTag(tag) {
  if (!confirm(`Delete tag "${tag.name}"?`)) return
  try {
    await tagStore.remove(tag.id)
    tags.value = tags.value.filter(t => t.id !== tag.id)
  } catch (e) {
    alert("Failed to delete tag: " + e.message)
  }
}

async function createTag() {
  if (!newTagName.value.trim()) {
    alert("Tag name cannot be empty")
    return
  }
  try {
    await tagStore.create(newTagName.value)
    newTagName.value = ''
    loadTags()
  } catch (e) {
    alert("Failed to create tag: " + e.message)
  }
}

onMounted(() => {
  loadTags()
})
</script>

<style scoped>
.tags-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}
.new-tag {
  margin-bottom: 20px;
}
.new-tag input {
  padding: 8px;
  width: 250px;
  margin-right: 10px;
}
.new-tag button {
  padding: 8px 12px;
}
.tags-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.tag-card {
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
}
.tag-info {
  display: flex;
  gap: 15px;
  margin-bottom: 10px;
}
.tag-actions button {
  margin-right: 10px;
}
.subscribers-list {
  margin-top: 10px;
  background: #f9f9f9;
  padding: 10px;
  border-radius: 4px;
}
.edit-tag input {
  margin-right: 10px;
}
</style>
