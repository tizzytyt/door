const API_BASE = import.meta.env.VITE_API_BASE || '/api'

export function resolveAvatarUrl(avatar) {
  if (!avatar) return ''
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) return avatar
  return API_BASE + (avatar.startsWith('/') ? avatar : `/${avatar}`)
}

export function avatarLetter(user) {
  const name = (user?.realName || user?.username || '').trim()
  return name ? name.charAt(0) : '?'
}
