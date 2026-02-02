import Stomp from 'stompjs'
import SockJS from 'sockjs-client'
import { WS_URL } from '@/http/URL'
import { notifyMsg } from '@/util/utils'
import { MESSAGE_TYPE } from '@/constants/MessageTipEnumConstant'
import { NoticeTypeEnum } from '@/constants/NoticeTypeConstants'
import { useGlobalStore } from '@/stores/global'
import { watch } from 'vue'

let stompClient: any = null

export const connectWebSocket = () => {
  const globalStore = useGlobalStore()

  // 监听登录状态变化
  watch(
    () => globalStore.global.isLogin,
    (isLogin) => {
      if (isLogin) {
        initStomp()
      } else {
        disconnect()
      }
    },
    { immediate: true }
  )
}

const initStomp = () => {
  const globalStore = useGlobalStore()
  const userId = globalStore.global.user?.userId

  if (!userId) {
    return
  }

  if (stompClient && stompClient.connected) {
    return
  }

  // 构造 WebSocket 地址，这里假设后端 WebSocket 挂载在 /ws 路径下
  // 如果 WS_URL 已经是完整路径，则直接使用，否则拼接 /ws
  // 考虑到 .env 中 VITE_WS_URL 可能配置的是 API 根路径
  // 尝试使用 WS_URL + '/ws'
  const serviceUrl = WS_URL.replace('ws://', 'http://').replace('wss://', 'https://') + '/ws'
  const socket = new SockJS(serviceUrl)
  stompClient = Stomp.over(socket)

  // 禁用调试日志，避免控制台刷屏
  stompClient.debug = null

  stompClient.connect(
    {},
    (frame: any) => {
      console.log('WebSocket connection established')

      // 订阅个人消息通知 /user/queue/notify
      stompClient.subscribe('/user/queue/notify', (message: any) => {
        console.log('Received notification:', message)

        // 更新全局消息数
        if (globalStore.global.msgNum !== null) {
          globalStore.global.msgNum++
        } else {
          globalStore.global.msgNum = 1
        }

        if (message.body) {
          try {
            // 尝试根据约定格式解析
            // 假设后端发送的格式暂定为 text 或者 JSON {title, content, type}
            // 如果只是简单字符串，直接展示
            if (message.body.startsWith('{')) {
              const msgData = JSON.parse(message.body)
              let title = msgData.title || '新消息'

              // 自动识别消息类型并设置友好标题
              const type = msgData.type || msgData.msgType || msgData.noticeType
              if (type) {
                switch (type) {
                  case NoticeTypeEnum.COMMENT_TYPE:
                    title = '收到新评论 📝'
                    break
                  case NoticeTypeEnum.REPLY_TYPE:
                    title = '收到新回复 💬'
                    break
                  case NoticeTypeEnum.PRAISE_TYPE:
                    title = '收到新点赞 👍'
                    break
                  case NoticeTypeEnum.COLLECT_TYPE:
                    title = '收到新收藏 ⭐'
                    break
                  case NoticeTypeEnum.FOLLOW_TYPE:
                    title = '新增关注 👤'
                    break
                  case NoticeTypeEnum.SYSTEM_TYPE:
                    title = '系统通知 🔔'
                    break
                }
              }

              notifyMsg(
                title,
                msgData.content || msgData.message || '您收到一条新消息',
                // 默认都使用 INFO 类型，除非后端明确指定了 error/success/warning
                ['success', 'warning', 'error'].includes(msgData.level || msgData.type)
                  ? msgData.level || msgData.type
                  : MESSAGE_TYPE.INFO
              )
            } else {
              notifyMsg('系统通知', message.body, MESSAGE_TYPE.INFO)
            }
          } catch (e) {
            console.error('Failed to parse notification', e)
            notifyMsg('系统通知', message.body, MESSAGE_TYPE.INFO)
          }
        }
      })
    },
    (error: any) => {
      console.error('WebSocket connection error:', error)
      // 可以在这里添加重连机制
      setTimeout(() => {
        if (globalStore.global.isLogin) {
          console.log('Attempting to reconnect WebSocket...')
          initStomp()
        }
      }, 5000)
    }
  )
}

const disconnect = () => {
  if (stompClient) {
    stompClient.disconnect(() => {
      console.log('WebSocket disconnected')
    })
    stompClient = null
  }
}
