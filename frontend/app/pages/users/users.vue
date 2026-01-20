<template>
  <div class="users-page">
    <button @click="goToPublications" class="btn-back-to-pubs">← Back to Publications</button>
    <h1>All Users</h1>

    <div v-if="loading" class="loading">Loading...</div>

    <div v-else-if="error" class="error">{{ error }}</div>

    <div v-else-if="users.length === 0" class="empty">
      <p>No users found</p>
    </div>

    <div v-else class="users-list">
      <div v-for="user in users" :key="user.id" class="user-card">
        <h3>{{ user.name }}</h3>
        <p class="email">{{ user.email }}</p>
        <button @click="goToUserDetails(user.username)" class="btn-details">Show Details</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '~/stores/user-store'
import { useAuthStore } from '~/stores/auth-store'

const userStore = useUserStore()
const authStore = useAuthStore()
const router = useRouter()
const users = ref([])
const loading = ref(true)
const error = ref(null)

useAuthErrorRedirect(error)

onMounted(async () => {
  if (!authStore.isAuthenticated || authStore.user?.role !== 'ADMINISTRADOR') {
    router.push('/publication/searchPublications')
    return
  }
  try {
    const data = await userStore.getAll()
    users.value = data
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
})

function goToUserDetails(userName) {
  router.push(`/users/userDetails?id=${userName}`)
}

function goToPublications() {
  router.push('/publication/searchPublications')
}
</script>

<style scoped>
.users-page {
  padding: 20px;
}

.btn-back-to-pubs {
  padding: 10px 20px;
  background: #0077cc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 20px;
  transition: background 0.2s;
}

.btn-back-to-pubs:hover {
  background: #005fa3;
}

.loading, .error, .empty {
  text-align: center;
  margin: 20px 0;
}

.users-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.user-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 15px;
  background: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.user-card h3 {
  margin: 0 0 10px 0;
}

.email {
  color: #666;
  margin: 0 0 15px 0;
}

.btn-details {
  padding: 8px 16px;
  background: #0077cc;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: background 0.2s;
}

.btn-details:hover {
  background: #005fa3;
}
</style>
