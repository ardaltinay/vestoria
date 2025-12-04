const COLORS = [
  '10b981', // emerald-500
  '3b82f6', // blue-500
  '6366f1', // indigo-500
  '8b5cf6', // violet-500
  'ec4899', // pink-500
  'f43f5e', // rose-500
  'f59e0b', // amber-500
  '0ea5e9', // sky-500
];

export function getAvatarUrl(username, size = 128) {
  if (!username) return `https://ui-avatars.com/api/?name=User&background=cbd5e1&color=fff&size=${size}`;

  // Simple hash to pick a consistent color
  let hash = 0;
  for (let i = 0; i < username.length; i++) {
    hash = username.charCodeAt(i) + ((hash << 5) - hash);
  }

  const colorIndex = Math.abs(hash) % COLORS.length;
  const background = COLORS[colorIndex];

  return `https://ui-avatars.com/api/?name=${encodeURIComponent(username)}&background=${background}&color=fff&size=${size}&font-size=0.4&bold=true`;
}
