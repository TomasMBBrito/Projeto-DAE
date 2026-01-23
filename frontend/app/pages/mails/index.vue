<template>
  <div class="emails-page">
    <nav class="navbar">
      <div class="navbar-brand">
        <h1>My Emails</h1>
      </div>
      <div class="navbar-actions">
        <button @click="goToPublications" class="btn-nav"><- Back to Publications</button>
        <button @click="goToProfile" class="btn-nav">Profile</button>
        <button @click="goLogout" class="btn-logout">Logout</button>
      </div>
    </nav>

    <div v-if="loading" class="loading">Loading emails...</div>

    <div v-else-if="error" class="error">{{ error }}</div>

    <div v-else-if="emails.length === 0" class="empty">
      No emails received
    </div>

    <div v-else class="emails-list">
      <div v-for="email in emails" :key="email.id" class="email-card">
        <div class="email-header">
          <span class="email-from">
            From: System
          </span>
          <span class="email-date">
            {{ formatDate(email.sentAt || email.date) }}
          </span>
        </div>

        <div class="email-subject">
          {{ email.subject }}
        </div>

        <div class="email-body">
          {{ email.body || email.content }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '~/stores/auth-store'

const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()

const emails = ref([])
const loading = ref(false)
const error = ref('')

onMounted(() => {
  loadEmails()
})

async function loadEmails() {
  loading.value = true
  error.value = ''

  try {
    emails.value = await userStore.getMyEmails()
    console.log(emails.value)
  } catch (e) {
    error.value = e.message || 'Failed to load emails'
  } finally {
    loading.value = false
  }
}

function goToPublications() {
  router.push('/publication/searchPublications')
}

function goToProfile() {
  const username = authStore.user?.username || authStore.username
  if (username) router.push(`/users/${username}`)
}

function goLogout() {
  router.push('/auth/login')
}

function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleString()
}
</script>

<style scoped>
.emails-page {
  font-family: "Inter", sans-serif;
  padding: 20px;
  max-width: 1100px;
  margin: 0 auto;
}

/* Navbar */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  margin-bottom: 25px;
  border-bottom: 2px solid #e0e0e0;
}

.navbar-brand h1 {
  margin: 0;
  color: #333;
  font-size: 22px;
}

.navbar-actions {
  display: flex;
  gap: 12px;
}

.btn-nav {
  background: transparent;
  color: #0077cc;
  border: 1px solid #0077cc;
  padding: 8px 14px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.btn-nav:hover {
  background: #0077cc;
  color: white;
}

.btn-logout {
  background: #dc3545;
  color: white;
  border: none;
  padding: 8px 14px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

/* Emails */
.emails-list {
  display: grid;
  gap: 16px;
}

.email-card {
  background: white;
  padding: 16px 18px;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.06);
  transition: transform .15s, box-shadow .15s;
}

.email-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.email-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
  color: #666;
}

.email-subject {
  font-weight: 700;
  font-size: 15px;
  margin-bottom: 6px;
  color: #0077cc;
}

.email-body {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}

.loading,
.error,
.empty {
  text-align: center;
  padding: 30px;
  font-size: 15px;
}

.error {
  color: #c33;
  background: #fee;
  border-radius: 6px;
}

.empty {
  color: #666;
}
</style>
