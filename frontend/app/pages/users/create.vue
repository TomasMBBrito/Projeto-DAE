<template>
    <div class="create-user-page">
        <div class="page-header">
            <div>
                <h1>Create New User</h1>
                <p class="page-subtitle">Add a new user to the platform</p>
            </div>
            <button @click="goBack" class="btn-back">← Back to Users</button>
        </div>

        <div class="alert alert-error" v-if="error">
            <div class="alert-icon">⚠</div>
            <div class="alert-content">{{ error }}</div>
        </div>

        <div class="alert alert-success" v-if="success">
            <div class="alert-icon">✓</div>
            <div class="alert-content">{{ success }}</div>
        </div>

        <form @submit.prevent="handleSubmit" class="user-form">
            <div class="form-section">
                <h2 class="section-title">Account Information</h2>
                
                <div class="form-row">
                    <div class="form-group">
                        <label for="username">Username <span class="required">*</span></label>
                        <input 
                            id="username"
                            v-model="formData.username" 
                            type="text" 
                            required 
                            placeholder="Enter username"
                            class="form-input"
                        />
                        <span class="form-hint">Unique identifier for the user</span>
                    </div>

                    <div class="form-group">
                        <label for="email">Email Address <span class="required">*</span></label>
                        <input 
                            id="email"
                            v-model="formData.email" 
                            type="email" 
                            required 
                            placeholder="user@example.com"
                            class="form-input"
                        />
                        <span class="form-hint">User's email for notifications</span>
                    </div>
                </div>

                <div class="form-group">
                    <label for="name">Full Name <span class="required">*</span></label>
                    <input 
                        id="name"
                        v-model="formData.name" 
                        type="text" 
                        required 
                        placeholder="Enter full name"
                        class="form-input"
                    />
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="password">Password <span class="required">*</span></label>
                        <input 
                            id="password"
                            v-model="formData.password" 
                            type="password" 
                            required 
                            placeholder="Minimum 6 characters"
                            minlength="6"
                            class="form-input"
                        />
                        <span class="form-hint">Minimum 6 characters</span>
                    </div>

                    <div class="form-group">
                        <label for="role">User Role <span class="required">*</span></label>
                        <select id="role" v-model="formData.role" required class="form-select">
                            <option value="" disabled>Select a role</option>
                            <option value="COLABORADOR">Colaborador</option>
                            <option value="RESPONSAVEL">Responsavel</option>
                            <option value="ADMINISTRADOR">Administrador</option>
                        </select>
                        <span class="form-hint">Determines user permissions</span>
                    </div>
                </div>
            </div>

            <div class="form-actions">
                <button type="button" @click="goBack" class="btn-secondary">Cancel</button>
                <button type="submit" class="btn-primary" :disabled="loading">
                    <span v-if="loading">Creating...</span>
                    <span v-else>Create User</span>
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
    font-family: "Inter", sans-serif;
    max-width: 800px;
    margin: 0 auto;
    padding: 30px 20px;
    background: #f9fafb;
    min-height: 100vh;
}

/* Page Header */
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 30px;
    padding-bottom: 20px;
    border-bottom: 2px solid #e2e8f0;
}

h1 {
    margin: 0 0 8px 0;
    color: #1e293b;
    font-size: 32px;
    font-weight: 700;
}

.page-subtitle {
    margin: 0;
    color: #64748b;
    font-size: 14px;
}

.btn-back {
    background: #64748b;
    color: white;
    border: none;
    padding: 10px 20px;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 600;
    font-size: 14px;
    transition: all 0.2s;
}

.btn-back:hover {
    background: #475569;
}

/* Alerts */
.alert {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 20px;
    border-radius: 8px;
    margin-bottom: 25px;
    font-size: 14px;
}

.alert-icon {
    font-size: 20px;
    font-weight: bold;
}

.alert-content {
    flex: 1;
}

.alert-error {
    background: #fef2f2;
    color: #dc2626;
    border: 1px solid #fecaca;
}

.alert-success {
    background: #f0fdf4;
    color: #16a34a;
    border: 1px solid #bbf7d0;
}

/* Form */
.user-form {
    background: white;
    padding: 35px;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.form-section {
    margin-bottom: 30px;
}

.section-title {
    margin: 0 0 25px 0;
    color: #1e293b;
    font-size: 18px;
    font-weight: 700;
    padding-bottom: 12px;
    border-bottom: 2px solid #e2e8f0;
}

.form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
}

.form-group {
    margin-bottom: 24px;
}

.form-group label {
    display: block;
    margin-bottom: 8px;
    font-weight: 600;
    color: #1e293b;
    font-size: 14px;
}

.required {
    color: #ef4444;
}

.form-input,
.form-select {
    width: 100%;
    padding: 12px 16px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    font-size: 14px;
    font-family: "Inter", sans-serif;
    box-sizing: border-box;
    transition: all 0.2s;
    background: white;
}

.form-input:focus,
.form-select:focus {
    outline: none;
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-select {
    cursor: pointer;
    appearance: none;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%2364748b' d='M6 9L1 4h10z'/%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-position: right 12px center;
    padding-right: 40px;
}

.form-hint {
    display: block;
    margin-top: 6px;
    font-size: 12px;
    color: #64748b;
}

/* Form Actions */
.form-actions {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
    padding-top: 30px;
    border-top: 1px solid #e2e8f0;
}

.btn-primary,
.btn-secondary {
    border: none;
    padding: 12px 28px;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 600;
    font-size: 14px;
    transition: all 0.2s;
}

.btn-secondary {
    background: #f1f5f9;
    color: #475569;
}

.btn-secondary:hover {
    background: #e2e8f0;
}

.btn-primary {
    background: #3b82f6;
    color: white;
}

.btn-primary:hover:not(:disabled) {
    background: #2563eb;
}

.btn-primary:disabled {
    background: #94a3b8;
    cursor: not-allowed;
}

/* Responsive */
@media (max-width: 768px) {
    .page-header {
        flex-direction: column;
        gap: 15px;
    }

    .btn-back {
        align-self: flex-start;
    }

    .form-row {
        grid-template-columns: 1fr;
    }

    .user-form {
        padding: 25px 20px;
    }

    h1 {
        font-size: 24px;
    }

    .form-actions {
        flex-direction: column-reverse;
    }

    .form-actions button {
        width: 100%;
    }
}
</style>