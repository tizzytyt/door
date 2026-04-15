const TOKEN_KEY = 'ac_admin_token'
const USER_KEY = 'ac_admin_user'

export const auth = {
  getToken() {
    return localStorage.getItem(TOKEN_KEY)
  },
  hasToken() {
    return !!localStorage.getItem(TOKEN_KEY)
  },
  setSession(token, user) {
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(USER_KEY, JSON.stringify(user ?? null))
  },
  getUser() {
    const raw = localStorage.getItem(USER_KEY)
    if (!raw) return null
    try {
      return JSON.parse(raw)
    } catch {
      return null
    }
  },
  clear() {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  },
}

