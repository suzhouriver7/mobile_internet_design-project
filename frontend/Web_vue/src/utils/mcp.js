// 一个非常轻量级的“本地 MCP 注册中心”，用于在前端给 AI 问询挂载可调用的服务
// 这里只负责：注册服务、按名称调用服务，其余由各个服务自己实现

const registry = {}

/**
 * 注册一个 MCP 服务
 * @param {string} name 唯一名称
 * @param {(ctx: any) => Promise<any> | any} handler 处理函数
 */
export function registerMcpService(name, handler) {
  if (!name || typeof handler !== 'function') return
  registry[name] = handler
}

/**
 * 调用 MCP 服务
 * @param {string} name 服务名
 * @param {any} context 上下文，例如当前对话历史、最新用户输入等
 */
export async function callMcpService(name, context) {
  const handler = registry[name]
  if (!handler) {
    throw new Error(`MCP 服务未注册: ${name}`)
  }
  return await handler(context)
}

/**
 * 获取当前已注册的 MCP 服务列表
 */
export function listMcpServices() {
  return Object.keys(registry)
}
