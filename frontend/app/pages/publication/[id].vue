<!-- pages/publication/[id].vue -->
<template>
  <div class="publication-details">
    <button @click="goBack" class="btn-back">← Back to Search</button>

    <div v-if="loading" class="loading">Loading...</div>

    <div v-else-if="error" class="error">{{ error }}</div>

    <div v-else-if="publication" class="publication-content">
      <div class="publication-header">
        <div class="header-top">
          <h1>{{ publication.title }}</h1>
          <div class="action-buttons">
            <button
                v-if="canEdit"
                @click="goToHistory"
                class="btn-history-pub"
                title="View History"
            >
              History
            </button>

            <button
                v-if="canEdit"
                @click="togglePublicationVisibility"
                class="btn-toggle-pub"
                :title="publication.visible ? 'Hide Publication' : 'Show Publication'"
            >
              {{ publication.visible ? 'Hide' : 'Show' }}
            </button>

            <button
                v-if="canEdit"
                @click="goToEdit"
                class="btn-edit-publication"
            >
              Edit
            </button>

            <button
                v-if="canEdit"
                @click="deletePublication"
                class="btn-delete-publication"
            >
              Delete
            </button>
          </div>
        </div>

        <div v-if="!publication.visible && canEdit" class="visibility-warning">
          This publication is currently hidden
        </div>

        <p class="authors">By {{ publication.authors && publication.authors.length > 0 ? publication.authors.join(', ') : (publication.authors || 'Unknown') }}</p>
        <div class="meta-info">
          <span>Area: {{ publication.scientificArea }}</span>
          <span>Date: {{ formatDate(publication.publicationDate) }}</span>
          <span>Average Rating: {{ publication.averageRating ? publication.averageRating.toFixed(1) : 'N/A' }}</span>
        </div>

        <div v-if="publication.tags && publication.tags.length > 0" class="tags-section">
          <h3>Tags</h3>
          <div class="tags-list">
            <span
                v-for="tag in publication.tags"
                :key="tag.id"
                class="tag-badge"
                @click="filterByTag(tag.id)"
            >
              {{ tag.name }}
            </span>
          </div>
        </div>
      </div>

      <div class="publication-body">
        <h2>Summary</h2>
        <p>{{ publication.summary }}</p>
      </div>

      <div class="rating-section">
        <h2>Rate this Publication</h2>
        <div class="stars">
          <button
              v-for="star in 5"
              :key="star"
              @click="ratePublication(star)"
              class="star-btn"
              :class="{ active: star <= userRating }"
          >
            ★
          </button>
        </div>
        <p v-if="ratingMessage" class="rating-message">{{ ratingMessage }}</p>
      </div>

      <div class="comments-section">
        <h2>Comments ({{ comments.length }})</h2>

        <div class="add-comment">
          <textarea
              v-model="newComment"
              placeholder="Add a comment..."
              class="comment-input"
              rows="3"
          ></textarea>
          <button @click="postComment" class="btn-comment" :disabled="!newComment.trim()">
            Post Comment
          </button>
        </div>

        <div v-if="comments.length === 0" class="no-comments">
          No comments yet. Be the first to comment!
        </div>

        <div v-else class="comments-list">
          <div v-for="comment in comments" :key="comment.id" class="comment" :class="{ hidden: !comment.visible }">
            <div class="comment-header">
              <div class="comment-info">
                <span class="comment-author">{{ comment.author }}</span>
                <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
                <span v-if="!comment.visible" class="hidden-badge">Hidden</span>
              </div>
              <button
                  v-if="canEditComment(comment)"
                  @click="toggleCommentVisibilityFunc(comment)"
                  class="btn-toggle-visibility"
                  :title="comment.visible ? 'Hide comment' : 'Show comment'"
              >
                {{ comment.visible ? 'Hide' : 'Show' }}
              </button>
              <button
                  v-if="canEditComment(comment)"
                  @click="startEdit(comment)"
                  class="btn-edit-comment"
              >
                Edit
              </button>
            </div>
            <div v-if="editingCommentId === comment.id" class="edit-comment">
              <textarea
                  v-model="editText"
                  class="comment-input"
                  rows="3"
              ></textarea>
              <button @click="saveEdit(comment)" class="btn-save">Save</button>
              <button @click="cancelEdit" class="btn-cancel">Cancel</button>
            </div>
            <p v-else class="comment-content">{{ comment.content }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { usePublicationStore } from '~/stores/publication-store';
import { useUserStore } from '~/stores/user-store';
import { useAuthStore } from '~/stores/auth-store';

const route = useRoute()
const router = useRouter()
const publicationStore = usePublicationStore()
const userStore = useUserStore()
const authStore = useAuthStore()

const publication = ref(null)
const comments = ref([])
const loading = ref(false)
const error = ref('')
const newComment = ref('')
const userRating = ref(0)
const ratingMessage = ref('')
const editingCommentId = ref(null)
const editText = ref('')

const publicationId = computed(() => parseInt(route.params.id))

const canEdit = computed(() => {
  if (!publication.value || !authStore.user) return false

  const role = authStore.user.role
  const isAuthor = publication.value.submitterUsername === authStore.user.username

  return isAuthor || role === 'RESPONSAVEL' || role === 'ADMINISTRADOR'
})

const canEditComment = (comment) => {
  if (!authStore.user) return false

  const role = authStore.user.role
  const isAuthor = comment.author === authStore.user.username

  return isAuthor || role === 'RESPONSAVEL' || role === 'ADMINISTRADOR'
}

onMounted(() => {
  loadPublication()
  loadComments()
})

async function loadPublication() {
  loading.value = true
  error.value = ''

  try {
    publication.value = await publicationStore.getById(publicationId.value)
    console.log(publication.value)
  } catch (e) {
    error.value = e.message || 'Failed to load publication'
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  try {
    comments.value = await publicationStore.getComments(publicationId.value)
  } catch (e) {
    console.error('Failed to load comments:', e)
  }
}

async function postComment() {
  if (!newComment.value.trim()) return

  try {
    await publicationStore.addComment(publicationId.value, newComment.value)
    newComment.value = ''
    await loadComments()
  } catch (e) {
    alert('Failed to post comment: ' + e.message)
  }
}

async function toggleCommentVisibilityFunc(comment) {
  try {
    const newVisibility = !comment.visible
    await publicationStore.toggleCommentVisibility(
        publicationId.value,
        comment.id,
        newVisibility
    )
    comment.visible = newVisibility
  } catch (e) {
    alert('Failed to toggle comment visibility: ' + e.message)
  }
}

function startEdit(comment) {
  editingCommentId.value = comment.id
  editText.value = comment.content
}

function cancelEdit() {
  editingCommentId.value = null
  editText.value = ''
}

async function saveEdit(comment) {
  if (!editText.value.trim()) return

  try {
    await publicationStore.editComment(publicationId.value, comment.id, editText.value)
    comment.content = editText.value
    editingCommentId.value = null
    editText.value = ''
  } catch (e) {
    alert('Failed to edit comment: ' + e.message)
  }
}

async function togglePublicationVisibility() {
  if (!confirm(`Are you sure you want to ${publication.value.visible ? 'hide' : 'show'} this publication?`)) {
    return
  }

  try {
    const newVisibility = !publication.value.visible
    await publicationStore.toggleVisibility(publicationId.value, newVisibility)
    publication.value.visible = newVisibility
    alert(`Publication ${newVisibility ? 'shown' : 'hidden'} successfully!`)
  } catch (e) {
    alert('Failed to toggle visibility: ' + e.message)
  }
}

async function deletePublication() {
  if (!confirm('Are you sure you want to DELETE this publication? This action cannot be undone!')) {
    return
  }

  try {
    await userStore.deleteMyPost(publicationId.value)
    alert('Publication deleted successfully!')
    router.push('/publication/searchPublications')
  } catch (e) {
    alert('Failed to delete publication: ' + e.message)
  }
}

function goToHistory() {
  router.push(`/publication/${publicationId.value}-history`)
}

async function ratePublication(rating) {
  userRating.value = rating
  ratingMessage.value = ''

  try {
    await publicationStore.addRating(publicationId.value, rating)
    ratingMessage.value = 'Rating submitted successfully!'
    await loadPublication()
  } catch (e) {
    ratingMessage.value = 'Failed to submit rating'
  }
}

function goBack() {
  router.push('/publication/searchPublications')
}

function goToEdit() {
  router.push(`/publication/details/${publicationId.value}`)
}

function filterByTag(tagId) {
  router.push({
    path: '/publication/searchPublications',
    query: { tagId: tagId }
  })
}

function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString()
}
</script>

<style scoped>
.publication-details {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.btn-back {
  background: #f0f0f0;
  border: none;
  padding: 10px 15px;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 20px;
  font-size: 14px;
}

.btn-back:hover {
  background: #e0e0e0;
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

.publication-content {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.publication-header h1 {
  margin: 0 0 10px 0;
  color: #333;
  flex: 1;
}

.action-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn-history-pub {
  padding: 10px 16px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  white-space: nowrap;
}

.btn-history-pub:hover {
  background: #5a6268;
}

.btn-toggle-pub {
  padding: 10px 16px;
  background: #ffc107;
  color: #333;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  white-space: nowrap;
}

.btn-toggle-pub:hover {
  background: #e0a800;
}

.btn-edit-publication {
  padding: 10px 20px;
  background: #0077cc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  white-space: nowrap;
}

.btn-edit-publication:hover {
  background: #005fa3;
}

.btn-delete-publication {
  padding: 10px 20px;
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  white-space: nowrap;
}

.btn-delete-publication:hover {
  background: #c82333;
}

.visibility-warning {
  background: #fff3cd;
  border: 1px solid #ffc107;
  color: #856404;
  padding: 10px 15px;
  border-radius: 4px;
  margin-bottom: 15px;
  font-weight: 600;
}

.authors {
  color: #666;
  font-style: italic;
  margin: 0 0 15px 0;
}

.meta-info {
  display: flex;
  gap: 20px;
  padding: 15px 0;
  border-bottom: 2px solid #eee;
  margin-bottom: 20px;
  font-size: 14px;
  color: #666;
}

.tags-section {
  margin-bottom: 30px;
}

.tags-section h3 {
  font-size: 16px;
  color: #333;
  margin: 0 0 12px 0;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-badge {
  padding: 6px 14px;
  background: #0077cc;
  color: white;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.tag-badge:hover {
  background: #005fa3;
  transform: translateY(-1px);
}

.publication-body {
  margin-bottom: 40px;
}

.publication-body h2 {
  color: #333;
  margin: 0 0 15px 0;
  font-size: 20px;
}

.publication-body p {
  line-height: 1.6;
  color: #333;
}

.rating-section {
  margin-bottom: 40px;
  padding-top: 30px;
  border-top: 2px solid #eee;
}

.rating-section h2 {
  margin: 0 0 15px 0;
  font-size: 20px;
}

.stars {
  display: flex;
  gap: 5px;
  margin-bottom: 10px;
}

.star-btn {
  background: none;
  border: none;
  font-size: 32px;
  cursor: pointer;
  color: #ddd;
  transition: color 0.2s;
}

.star-btn.active,
.star-btn:hover {
  color: #ffc107;
}

.rating-message {
  color: #0077cc;
  font-size: 14px;
  margin: 10px 0 0 0;
}

.comments-section {
  padding-top: 30px;
  border-top: 2px solid #eee;
}

.comments-section h2 {
  margin: 0 0 20px 0;
  font-size: 20px;
}

.add-comment {
  margin-bottom: 30px;
}

.comment-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: Arial, sans-serif;
  resize: vertical;
  box-sizing: border-box;
}

.comment-input:focus {
  outline: none;
  border-color: #0077cc;
}

.btn-comment {
  margin-top: 10px;
  padding: 10px 20px;
  background: #0077cc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.btn-comment:hover:not(:disabled) {
  background: #005fa3;
}

.btn-comment:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.no-comments {
  text-align: center;
  padding: 40px;
  color: #666;
  font-style: italic;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment {
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
  border-left: 3px solid #0077cc;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.comment-author {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.comment-date {
  color: #999;
  font-size: 12px;
}

.comment.hidden .comment-content {
  color: #666;
}

.comment-content {
  margin: 0;
  line-height: 1.5;
  color: #333;
  font-size: 14px;
}

.hidden-badge {
  background: #ff9800;
  color: white;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
}

.btn-toggle-visibility {
  background: none;
  border: 1px solid #ddd;
  padding: 4px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.2s;
}

.btn-toggle-visibility:hover {
  background: #f0f0f0;
  border-color: #0077cc;
}

.btn-edit-comment {
  background: #28a745;
  color: white;
  border: none;
  padding: 4px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin-left: 5px;
  transition: background 0.2s;
}

.btn-edit-comment:hover {
  background: #218838;
}

.edit-comment {
  margin-top: 10px;
}

.btn-save {
  background: #0077cc;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin-right: 5px;
  margin-top: 5px;
}

.btn-save:hover {
  background: #005fa3;
}

.btn-cancel {
  background: #6c757d;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin-top: 5px;
}

.btn-cancel:hover {
  background: #545b62;
}
</style>