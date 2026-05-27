const { BASE_URL } = require('./request.js');

function resolveAvatarUrl(avatar) {
  if (!avatar) {
    return '';
  }
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar;
  }
  return BASE_URL + (avatar.startsWith('/') ? avatar : `/${avatar}`);
}

function avatarLetter(user) {
  if (!user) return '?';
  const name = (user.realName || user.username || '').trim();
  return name ? name.charAt(0) : '?';
}

module.exports = {
  resolveAvatarUrl,
  avatarLetter
};
