<!-- pages/publication/[id].vue -->
<template>
  <div class="publication-details">
    <button @click="goBack" class="btn-back">← Back to Publications</button>

    <div v-if="loading" class="loading">Loading...</div>

    <div v-else-if="error" class="error">{{ error }}</div>

    <div v-else-if="publication" class="publication-content">
      <div class="publication-header">
        <h1>{{ publication.title }}</h1>
        <p class="authors">By {{ publication.authors }}</p>
        <div class="meta-info">
          <span>Area: {{ publication.scientificArea }}</span>
          <span>Date: {{ formatDate(publication.publicationDate) }}</span>
          <span>Average Rating: {{ publication.averageRating ? publication.averageRating.toFixed(1) : 'N/A' }}</span>
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
          <div v-for="comment in comments" :key="comment.id" class="comment">
            <div class="comment-header">
              <span class="comment-author">{{ comment.authorName }}</span>
              <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
            </div>
            <p class="comment-content">{{ comment.content }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { usePublicationStore } from '~/stores/publication-store';
import { useAuthStore } from '~/stores/auth-store';

const route = useRoute()
const router = useRouter()
const publicationStore = usePublicationStore()
const authStore = useAuthStore()

const publication = ref(null)
const comments = ref([])
const loading = ref(false)
const error = ref('')

useAuthErrorRedirect(error)
const newComment = ref('')
const userRating = ref(0)
const ratingMessage = ref('')

const publicationId = computed(() => parseInt(route.params.id))

onMounted(() => {
  if (!authStore.isAuthenticated || authStore.user?.role !== 'ADMINISTRADOR') {
    router.push('/publication/searchPublications')
    return
  }
  loadPublication()
  loadComments()
})

async function loadPublication() {
  loading.value = true
  error.value = ''
  
  try {
    publication.value = await publicationStore.getById(publicationId.value)
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

.publication-header h1 {
  margin: 0 0 10px 0;
  color: #333;
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
  margin-bottom: 30px;
  font-size: 14px;
  color: #666;
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
  margin-bottom: 8px;
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

.comment-content {
  margin: 0;
  line-height: 1.5;
  color: #333;
  font-size: 14px;
}
</style>