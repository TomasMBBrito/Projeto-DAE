<template>
    <div class="create-user-page">
        <div class="page-header">
            <h1>Create New User</h1>
            <button @click="goBack" class="btn-back">← Back to Users</button>
        </div>

        <div class="error" v-if="error">{{ error }}</div>
        <div class="success" v-if="success">{{ success }}</div>

        <form @submit.prevent="handleSubmit" class="user-form">
            <div class="form-group">
                <label for="username">Username *</label>
                <input 
                    id="username"
                    v-model="formData.username" 
                    type="text" 
                    required 
                    placeholder="Enter username"
                />
            </div>

            <div class="form-group">
                <label for="name">Full Name *</label>
                <input 
                    id="name"
                    v-model="formData.name" 
                    type="text" 
                    required 
                    placeholder="Enter full name"
                />
            </div>

            <div class="form-group">
                <label for="email">Email *</label>
                <input 
                    id="email"
                    v-model="formData.email" 
                    type="email" 
                    required 
                    placeholder="Enter email address"
                />
            </div>

            <div class="form-group">
                <label for="password">Password *</label>
                <input 
                    id="password"
                    v-model="formData.password" 
                    type="password" 
                    required 
                    placeholder="Enter password"
                    minlength="6"
                />
            </div>

            <div class="form-group">
                <label for="role">Role *</label>
                <select id="role" v-model="formData.role" required>
                    <option value="">Select a role</option>
                    <option value="COLABORADOR">COLABORADOR</option>
                    <option value="RESPONSAVEL">RESPONSAVEL</option>
                    <option value="ADMINISTRADOR">ADMINISTRADOR</option>
                </select>
            </div>

            <div class="form-actions">
                <button type="button" @click="goBack" class="btn-cancel">Cancel</button>
                <button type="submit" class="btn-submit" :disabled="loading">
                    {{ loading ? 'Creating...' : 'Create User' }}
                </button>
            </div>
        </form>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '~/stores/user-store'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const formData = ref({
    username: '',
    name: '',
    email: '',
    password: '',
    role: ''
})

const loading = ref(false)
const error = ref(null)
const success = ref(null)

async function handleSubmit() {
    loading.value = true
    error.value = null
    success.value = null

    try {
        const response = await userStore.create(formData.value)
        success.value = response.message || 'User created successfully!'
        
        // Reset form
        formData.value = {
            username: '',
            name: '',
            email: '',
            password: '',
            role: ''
        }

        // Redirect after 2 seconds
        setTimeout(() => {
            router.push('/users')
        }, 2000)

    } catch (e) {
        if (e.data?.message) {
            error.value = e.data.message
        } else {
            error.value = 'Failed to create user. Please try again.'
        }
        console.error(e)
    } finally {
        loading.value = false
    }
}

function goBack() {
    router.push('/users')
}
</script>

<style scoped>
.create-user-page {
    max-width: 600px;
    margin: 0 auto;
    padding: 20px;
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;
}

.page-header h1 {
    margin: 0;
}

.btn-back {
    background: #6c757d;
    color: white;
    border: none;
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
    font-weight: 600;
}

.btn-back:hover {
    background: #5a6268;
}

/* Form styles */
.user-form {
    background: white;
    padding: 30px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    margin-bottom: 8px;
    font-weight: 600;
    color: #333;
}

.form-group input,
.form-group select {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 14px;
    box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus {
    outline: none;
    border-color: #007bff;
}

.form-group select {
    cursor: pointer;
}

/* Form actions */
.form-actions {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #eee;
}

.btn-cancel {
    background: #6c757d;
    color: white;
    border: none;
    padding: 10px 24px;
    border-radius: 4px;
    cursor: pointer;
    font-weight: 600;
}

.btn-cancel:hover {
    background: #5a6268;
}

.btn-submit {
    background: #007bff;
    color: white;
    border: none;
    padding: 10px 24px;
    border-radius: 4px;
    cursor: pointer;
    font-weight: 600;
}

.btn-submit:hover:not(:disabled) {
    background: #0056b3;
}

.btn-submit:disabled {
    background: #ccc;
    cursor: not-allowed;
}

/* Messages */
.error {
    background: #fee;
    color: #dc3545;
    border-radius: 6px;
    padding: 12px 16px;
    margin-bottom: 20px;
    border: 1px solid #fcc;
}

.success {
    background: #d4edda;
    color: #155724;
    border-radius: 6px;
    padding: 12px 16px;
    margin-bottom: 20px;
    border: 1px solid #c3e6cb;
}
</style>