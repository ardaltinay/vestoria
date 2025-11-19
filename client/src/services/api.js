// Simple API utility: routes frontend requests starting with `/api` to the backend.
// Base URL is configurable via `VITE_API_BASE`. Defaults to http://localhost:8081

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8081'

function buildUrl(path) {
  // Only rewrite requests that begin with `/api`
  if (!path || typeof path !== 'string') return API_BASE
  if (path.startsWith('/api')) {
    return API_BASE.replace(/\/$/, '') + path
  }
  return path
}

async function request(path, { method = 'GET', data = null, headers = {} } = {}) {
  const url = buildUrl(path)
  const opts = { method, headers: { ...headers } }

  // use cookies for authentication (HttpOnly cookies set by backend)
  opts.credentials = 'include'

  if (data !== null && method !== 'GET' && method !== 'HEAD') {
    if (!(data instanceof FormData)) {
      opts.headers['Content-Type'] = 'application/json'
      opts.body = JSON.stringify(data)
    } else {
      // allow multipart form data
      delete opts.headers['Content-Type']
      opts.body = data
    }
  }

  const res = await fetch(url, opts)
  const contentType = res.headers.get('content-type') || ''
  if (!res.ok) {
    let body = null
    try {
      body = contentType.includes('application/json') ? await res.json() : await res.text()
    } catch {
      body = null
    }
    const err = new Error(res.statusText || 'HTTP error')
    err.status = res.status
    err.body = body
    throw err
  }

  if (contentType.includes('application/json')) return res.json()
  return res.text()
}

export const api = {
  get: (path, headers) => request(path, { method: 'GET', headers }),
  post: (path, data, headers) => request(path, { method: 'POST', data, headers }),
  put: (path, data, headers) => request(path, { method: 'PUT', data, headers }),
  del: (path, headers) => request(path, { method: 'DELETE', headers }),
  request,
}

export default api
