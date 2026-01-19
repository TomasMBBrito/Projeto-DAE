<script setup>
import { useAuthStore } from "~/stores/auth-store.js";

const router = useRouter()
const authStore = useAuthStore()
const { user, isAuthenticated } = storeToRefs(authStore)

function logout() {
  authStore.logout()
  router.push('/')
}
</script>

<template>
  <div class="layout-container">
    <nav class="navbar">
      <div class="nav-left">
        <nuxt-link to="/" class="nav-title">Academics</nuxt-link>

        <nuxt-link to="/auth-test" class="nav-link">Test</nuxt-link>
        <nuxt-link to="/auth/login" class="nav-link">Login</nuxt-link>
        <nuxt-link to="/students" class="nav-link">Students</nuxt-link>
      </div>

      <div class="nav-right">
        <template v-if="isAuthenticated">
          <span class="user-info">
            Welcome, {{ user?.name }} ({{ user?.role }})
          </span>
          <button class="logout-btn" @click.prevent="logout">Logout</button>
        </template>

        <template v-else>
          <span class="not-logged">Not logged in</span>
        </template>
      </div>
    </nav>

    <main class="content">
      <slot />
    </main>
  </div>
</template>

<style scoped>
/* Layout global */
.layout-container {
  font-family: Arial, sans-serif;
  background: #f5f5f5;
  min-height: 100vh;
}

/* Navbar */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #0077cc;
  padding: 15px 30px;
  color: white;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
}

.nav-left,
.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* Branding */
.nav-title {
  font-size: 22px;
  font-weight: bold;
  color: white;
  text-decoration: none;
  margin-right: 20px;
}

/* Links */
.nav-link {
  color: #eaf4ff;
  text-decoration: none;
  font-size: 15px;
}

.nav-link:hover {
  text-decoration: underline;
}

/* User info */
.user-info {
  font-size: 14px;
  margin-right: 10px;
}

/* Logout button */
.logout-btn {
  background: white;
  color: #0077cc;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
}

.logout-btn:hover {
  background: #e1e1e1;
}

/* Not logged in */
.not-logged {
  font-size: 14px;
  opacity: 0.8;
}

/* Main content */
.content {
  max-width: 1000px;
  margin: 40px auto;
  padding: 20px;
}
</style>
