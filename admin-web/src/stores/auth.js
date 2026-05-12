import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi } from '../api/admin'

const TOKEN_KEY = 'restaurant_admin_token'
const USER_KEY = 'restaurant_admin_user'
const TOKEN_EXPIRES_AT_KEY = 'restaurant_admin_token_expires_at'
const TOKEN_TTL_MS = 11.5 * 60 * 60 * 1000

function clearStoredAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  localStorage.removeItem(TOKEN_EXPIRES_AT_KEY)
}

function readInitialAuth() {
  const token = localStorage.getItem(TOKEN_KEY) || ''
  const expiresAt = Number(localStorage.getItem(TOKEN_EXPIRES_AT_KEY) || 0)
  if (!token || !expiresAt || Date.now() > expiresAt) {
    clearStoredAuth()
    return {
      token: '',
      user: null,
    }
  }
  let user = null
  try {
    user = JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  } catch (error) {
    clearStoredAuth()
    return {
      token: '',
      user: null,
    }
  }
  return {
    token,
    user,
  }
}

export const useAuthStore = defineStore('auth', () => {
  const initialAuth = readInitialAuth()
  const token = ref(initialAuth.token)
  const user = ref(initialAuth.user)
  const isLoggedIn = computed(() => Boolean(token.value))

  async function login(form) {
    const result = await loginApi(form)
    token.value = result.token
    user.value = result.user
    localStorage.setItem(TOKEN_KEY, result.token)
    localStorage.setItem(USER_KEY, JSON.stringify(result.user))
    localStorage.setItem(TOKEN_EXPIRES_AT_KEY, String(Date.now() + TOKEN_TTL_MS))
    return result
  }

  function logout() {
    token.value = ''
    user.value = null
    clearStoredAuth()
  }

  return {
    token,
    user,
    isLoggedIn,
    login,
    logout,
  }
})
