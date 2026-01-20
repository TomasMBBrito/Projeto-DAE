<template>
  <div class="users-page">
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
        <!-- Add more fields as needed -->
      </div>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '~/stores/user-store'

const userStore = useUserStore()
const users = ref([])
const loading = ref(true)
const error = ref(null)

onMounted(async () => {
  try {
    const data = await userStore.getAll()
    users.value = data
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.users-page {
  padding: 20px;
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
  margin: 0;
}
</style>
