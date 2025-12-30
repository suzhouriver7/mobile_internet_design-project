/**
 * 枚举常量定义
 * 与后端 enums 包中的枚举一一对应
 */

// 用户类型 - 对应 UserType
export const USER_TYPE = {
  COMMON: 0, // 普通用户
  ADMIN: 1   // 管理员
}

// 活动类型 - 对应 ActivityType
export const ACTIVITY_TYPE = {
  BASKETBALL: 0,  // 篮球
  BADMINTON: 1,   // 羽毛球
  MEAL: 2,        // 吃饭
  STUDY: 3,       // 自习
  MOVIE: 4,       // 看电影
  RUNNING: 5,     // 跑步
  GAME: 6,        // 游戏
  OTHER: 7        // 其他活动
}

// 活动类型中文映射
export const ACTIVITY_TYPE_MAP = {
  0: '篮球',
  1: '羽毛球',
  2: '吃饭',
  3: '自习',
  4: '看电影',
  5: '跑步',
  6: '游戏',
  7: '其他'
}

// 性别要求 - 对应 GenderRequire
export const GENDER_REQUIRE = {
  MALE: 0,    // 男
  FEMALE: 1,  // 女
  ANY: 2      // 不限
}

export const GENDER_REQUIRE_MAP = {
  0: '仅限男生',
  1: '仅限女生',
  2: '性别不限'
}

// 校区 - 对应 Campus
export const CAMPUS = {
  LIANGXIANG: 0,      // 良乡校区
  ZHONGGUANCUN: 1,    // 中关村校区
  ZHUHAI: 2,          // 珠海校区
  XISHAN: 3,          // 西山校区
  OTHER_CAMPUS: 4     // 其他校区
}

export const CAMPUS_MAP = {
  0: '良乡校区',
  1: '中关村校区',
  2: '珠海校区',
  3: '西山校区',
  4: '其他校区'
}

// 订单状态 - 对应 OrderStatus
export const ORDER_STATUS = {
  PENDING: 0,       // 待匹配
  IN_PROGRESS: 1,   // 进行中
  COMPLETED: 2,     // 已完成
  CANCELLED: 3,     // 已取消
  EXPIRED: 4        // 已过期
}

export const ORDER_STATUS_MAP = {
  0: '待匹配',
  1: '进行中',
  2: '已完成',
  3: '已取消',
  4: '已过期'
}

// 申请状态 - 对应 ApplyStatus
export const APPLY_STATUS = {
  PENDING_REVIEW: 0,    // 待审核
  APPROVED: 1,          // 已通过
  REJECTED: 2,          // 已拒绝
  CANCELLED_APPLY: 3    // 已取消
}

export const APPLY_STATUS_MAP = {
  0: '待审核',
  1: '已通过',
  2: '已拒绝',
  3: '已取消'
}

// 内容类型 - 对应 PostType
export const POST_TYPE = {
  POST: 0,      // 动态
  COMMENT: 1    // 评论
}

// 媒体类型 - 对应 MediaType
export const MEDIA_TYPE = {
  TEXT_ONLY: 0, // 纯文本
  IMAGE: 1,     // 图片
  VIDEO: 2      // 视频
}

// 内容状态 - 对应 ContentStatus
export const CONTENT_STATUS = {
  NORMAL: 0,    // 正常
  DELETED: 1,   // 已删除
  PENDING: 2,   // 待审核
  REJECTED: 3   // 审核不通过
}

// 用户状态 - 对应 UserStatus
export const USER_STATUS = {
  ONLINE: 0,        // 在线
  OFFLINE: 1,       // 离线
  REGISTERING: 2,   // 注册中
  BANNED: 3         // 封禁中
}