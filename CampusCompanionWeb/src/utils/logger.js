// 简单前端日志工具，负责记录用户操作和系统事件

const MAX_LOGS = 500
const STORAGE_KEY = 'appLogs'

function loadLogs() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return []
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed : []
  } catch (e) {
    console.warn('[Logger] Failed to load logs from localStorage', e)
    return []
  }
}

function saveLogs(logs) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(logs.slice(-MAX_LOGS)))
  } catch (e) {
    // 本地存储失败时只在控制台警告，不影响正常功能
    console.warn('[Logger] Failed to save logs to localStorage', e)
  }
}

let logsCache = loadLogs()
const subscribers = []

function notifySubscribers(entry) {
  subscribers.forEach((fn) => {
    try {
      fn(entry)
    } catch (e) {
      console.warn('[Logger] subscriber error', e)
    }
  })
}

function createEntry(level, event, payload) {
  return {
    level,
    event,
    payload,
    timestamp: new Date().toISOString(),
    env: import.meta.env.MODE
  }
}

function log(level, event, payload = {}) {
  const entry = createEntry(level, event, payload)

  // 控制台输出，便于开发调试
  // 使用分级输出，保证在不同浏览器中可读性
  const prefix = `[${entry.timestamp}] [${level.toUpperCase()}] ${event}`
  if (level === 'error') {
    console.error(prefix, payload)
  } else if (level === 'warn') {
    console.warn(prefix, payload)
  } else {
    console.log(prefix, payload)
  }

  // 写入本地缓存
  logsCache.push(entry)
  if (logsCache.length > MAX_LOGS) {
    logsCache = logsCache.slice(-MAX_LOGS)
  }
  saveLogs(logsCache)
  notifySubscribers(entry)
}

const logger = {
  info(event, payload) {
    log('info', event, payload)
  },
  warn(event, payload) {
    log('warn', event, payload)
  },
  error(event, payload) {
    log('error', event, payload)
  },
  debug(event, payload) {
    if (import.meta.env.DEV) {
      log('debug', event, payload)
    }
  },
  event(name, payload) {
    log('info', `EVENT:${name}`, payload)
  },
  getLogs() {
    return [...logsCache]
  },
  clear() {
    logsCache = []
    saveLogs(logsCache)
  },
  subscribe(fn) {
    if (typeof fn === 'function') {
      subscribers.push(fn)
    }
    return () => {
      const index = subscribers.indexOf(fn)
      if (index >= 0) subscribers.splice(index, 1)
    }
  }
}

export default logger
