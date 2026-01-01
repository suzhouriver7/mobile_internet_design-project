// 针对当前前端的“订单创建”页面的一个本地 MCP 服务
// 职责：从最近一轮对话中「解析」出订单草稿，并返回一个用于自动填充表单的结构

import { reactive } from 'vue'

// 简单的中文关键词 + 正则解析，不依赖后端
function extractOrderDraftFromText(text) {
  if (!text) return null
  const draft = {
    activityType: '',
    campus: '',
    location: '',
    startTime: '',
    genderRequire: 'ANY',
    maxPeople: 2,
    note: ''
  }

  const lower = text.toLowerCase()

  // 活动类型
  if (text.includes('篮球')) draft.activityType = 'BASKETBALL'
  else if (text.includes('羽毛球')) draft.activityType = 'BADMINTON'
  else if (text.includes('吃饭') || text.includes('聚餐')) draft.activityType = 'MEAL'
  else if (text.includes('自习') || text.includes('学习')) draft.activityType = 'STUDY'
  else if (text.includes('电影')) draft.activityType = 'MOVIE'
  else if (text.includes('跑步') || text.includes('跑个步')) draft.activityType = 'RUNNING'
  else if (text.includes('游戏') || text.includes('开黑')) draft.activityType = 'GAME'

  // 校区
  if (text.includes('良乡')) draft.campus = 'LIANGXIANG'
  else if (text.includes('中关村')) draft.campus = 'ZHONGGUANCUN'
  else if (text.includes('珠海')) draft.campus = 'ZHUHAI'
  else if (text.includes('西山')) draft.campus = 'XISHAN'

  // 性别要求
  if (text.includes('只要女生') || text.includes('仅限女生') || text.includes('女生优先')) draft.genderRequire = 'FEMALE'
  else if (text.includes('只要男生') || text.includes('仅限男生') || text.includes('男生优先')) draft.genderRequire = 'MALE'

  // 人数：匹配“3人、3 个 人”等
  const peopleMatch = text.match(/(\d{1,2})\s*(人|位|个)/)
  if (peopleMatch) {
    const num = parseInt(peopleMatch[1], 10)
    if (!Number.isNaN(num) && num > 0 && num <= 20) {
      draft.maxPeople = num
    }
  }

  // 地点：非常简单地从“在XXX”后截取一段
  const locMatch = text.match(/在([\u4e00-\u9fa5A-Za-z0-9\s]{2,20})/)
  if (locMatch) {
    draft.location = locMatch[1].trim()
  }

  // 开始时间：这里只支持“今天/明天/后天 + HH点/HH:MM”这种非常有限的表达
  const day = (() => {
    if (text.includes('今天')) return 0
    if (text.includes('明天')) return 1
    if (text.includes('后天')) return 2
    return null
  })()

  const timeMatch = text.match(/(\d{1,2})[:点](\d{1,2})?/)
  if (day !== null && timeMatch) {
    const now = new Date()
    const target = new Date(now.getFullYear(), now.getMonth(), now.getDate() + day)
    const h = parseInt(timeMatch[1], 10)
    const m = timeMatch[2] ? parseInt(timeMatch[2], 10) : 0
    if (!Number.isNaN(h) && h >= 0 && h < 24 && !Number.isNaN(m) && m >= 0 && m < 60) {
      target.setHours(h, m, 0, 0)
      const y = target.getFullYear()
      const mon = String(target.getMonth() + 1).padStart(2, '0')
      const d = String(target.getDate()).padStart(2, '0')
      const hh = String(target.getHours()).padStart(2, '0')
      const mm = String(target.getMinutes()).padStart(2, '0')
      draft.startTime = `${y}-${mon}-${d} ${hh}:${mm}:00`
    }
  }

  // 备注：兜底直接放原始句子
  draft.note = text

  return draft
}

/**
 * MCP 服务入口
 * @param {{ messages?: Array<{role: string, content: string}>, inputText?: string, router?: any }} ctx
 *        ctx.messages 为当前 AI 对话历史
 *        ctx.inputText 为当前输入框里的最新文本（优先使用）
 */
export async function orderAssistantService(ctx) {
  const messages = ctx?.messages || []
  const router = ctx?.router || null
  const inputText = (ctx?.inputText || '').trim()

  if (!messages.length && !inputText) throw new Error('没有可用的对话内容，请先描述你的预约需求')

  let sourceText = ''

  // 1. 优先使用当前输入框的内容（用户常常输入完直接点“智能生成订单”而不发消息）
  if (inputText) {
    sourceText = inputText
  } else if (messages.length) {
    // 2. 否则回退到对话中最后一条 user 消息
    let lastUser = null
    for (let i = messages.length - 1; i >= 0; i--) {
      if (messages[i].role === 'user') {
        lastUser = messages[i]
        break
      }
    }

    if (!lastUser) {
      throw new Error('未找到可用的用户输入，请先在输入框描述你的需求')
    }

    sourceText = lastUser.content || ''
  }

  const draft = extractOrderDraftFromText(sourceText)
  if (!draft) {
    throw new Error('未能从对话中解析出订单信息，请再描述清楚一些～')
  }

  // 把解析结果通过路由参数带到创建订单页，由创建页自行合并到表单
  const query = {}
  if (draft.activityType) query.activityType = draft.activityType
  if (draft.campus) query.campus = draft.campus
  if (draft.location) query.location = draft.location
  if (draft.startTime) query.startTime = draft.startTime
  if (draft.genderRequire) query.genderRequire = draft.genderRequire
  if (draft.maxPeople) query.maxPeople = String(draft.maxPeople)
  if (draft.note) query.note = draft.note

  if (router && typeof router.push === 'function') {
    await router.push({ path: '/orders/create', query })
  }

  return { type: 'navigate', target: 'create-order', draft, query }
}

export function useOrderAssistantMcp() {
  // 预留给将来在组件里直接使用（如果需要的话）
  return reactive({ run: orderAssistantService })
}
