<template>
  <div class="message-list-container">
    <div class="message-list" ref="messageContainer" @scroll="handleScroll">
      <div
        v-for="(message, index) in chatStore.messages"
        :key="index"
        :class="['message', message.role === 'user' ? 'user-message' : 'bot-message']"
      >
        <div class="message-avatar">
          <el-icon v-if="message.role === 'user'" class="avatar-icon user">
            <User />
          </el-icon>
          <el-icon v-else class="avatar-icon bot">
            <ChatDotRound />
          </el-icon>
        </div>

        <div class="message-content">
          <div class="message-text" v-html="renderMarkdown(message.content)"></div>

          <!-- 流式加载动画 -->
          <div v-if="message.isStreaming" class="loading-dots">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 滚动到底部按钮 -->
    <transition name="fade">
      <el-button
        v-show="showScrollButton"
        class="scroll-to-bottom-btn"
        type="primary"
        circle
        @click="scrollToBottom"
      >
        <el-icon><ArrowDown /></el-icon>
      </el-button>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { useChatStore } from '@/stores/chatStore'
import { User, ChatDotRound, ArrowDown } from '@element-plus/icons-vue'
import { marked } from 'marked'

const chatStore = useChatStore()
const messageContainer = ref<HTMLElement>()
const showScrollButton = ref(false)

// 渲染 Markdown
function renderMarkdown(content: string): string {
  return marked(content) as string
}

// 检查是否在底部
function isNearBottom(): boolean {
  if (!messageContainer.value) return true
  const { scrollTop, scrollHeight, clientHeight } = messageContainer.value
  // 距离底部小于 100px 认为是在底部
  return scrollHeight - scrollTop - clientHeight < 100
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  })
}

// 处理滚动事件
function handleScroll() {
  showScrollButton.value = !isNearBottom()
}

// 监听消息变化，自动滚动（仅当用户在底部时）
watch(() => chatStore.messages.length, () => {
  // 新消息时，如果用户在底部或者没有手动滚动，自动滚动到底部
  if (isNearBottom()) {
    scrollToBottom()
  }
})

watch(() => chatStore.messages[chatStore.messages.length - 1]?.content, () => {
  // LLM 流式回复时，如果用户在底部，持续滚动到底部
  if (isNearBottom()) {
    scrollToBottom()
  }
}, { deep: true })
</script>

<style scoped>
.message-list-container {
  flex: 1;
  position: relative;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  scroll-behavior: smooth;
  background: var(--pai-neu-surface);
  min-height: 0;
}

.scroll-to-bottom-btn {
  position: absolute;
  bottom: 20px;
  right: 30px;
  z-index: 100;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  width: 40px;
  height: 40px;
}

.scroll-to-bottom-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

/* 淡入淡出动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  width: 100%;
}

@media (min-width: 1200px) {
  .message {
    max-width: 1200px;
    margin-left: auto;
    margin-right: auto;
  }
}

.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.avatar-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.avatar-icon.user {
  background: var(--pai-brand-1-normal);
  color: white;
}

.avatar-icon.bot {
  background: var(--pai-neu-surface-soft);
  color: var(--pai-color-3-gray);
}

.message-content {
  max-width: 75%;
  padding: 10px 14px;
  border-radius: 8px;
  line-height: 1.6;
  font-size: 14px;
}

.user-message .message-content {
  background: var(--pai-brand-1-normal);
  color: white;
}

.bot-message .message-content {
  background: var(--pai-neu-surface-soft);
  color: var(--pai-color-3-black);
  border: 1px solid var(--pai-neu-border);
}

.message-text {
  word-wrap: break-word;
}

.message-text :deep(p) {
  margin: 0 0 8px 0;
}

.message-text :deep(p:last-child) {
  margin-bottom: 0;
}

.message-text :deep(code) {
  background: rgba(0, 0, 0, 0.08);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

.message-text :deep(pre) {
  background: rgba(0, 0, 0, 0.05);
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 8px 0;
}

.message-text :deep(pre code) {
  background: none;
  padding: 0;
}

.loading-dots {
  display: inline-flex;
  gap: 4px;
  margin-left: 8px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--pai-color-3-gray);
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}

.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.message-list::-webkit-scrollbar {
  width: 8px;
}

.message-list::-webkit-scrollbar-thumb {
  background: var(--pai-color-5-gray);
  border-radius: 4px;
}

.message-list::-webkit-scrollbar-thumb:hover {
  background: var(--pai-color-3-gray);
}

html.dark .avatar-icon.user,
html.dark .user-message .message-content {
  background: #353535;
  color: #f2f2f2;
}

html.dark .message-text :deep(code) {
  background: rgba(255, 255, 255, 0.08);
  color: #f2f2f2;
}

html.dark .message-text :deep(pre) {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--pai-neu-border);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-list {
    padding: 12px;
  }

  .message {
    gap: 8px;
    margin-bottom: 16px;
  }

  .avatar-icon {
    width: 30px;
    height: 30px;
    font-size: 16px;
  }

  .message-content {
    max-width: 80%;
    padding: 8px 12px;
    font-size: 13px;
  }

  .message-text :deep(code) {
    font-size: 0.85em;
  }

  .scroll-to-bottom-btn {
    bottom: 15px;
    right: 15px;
    width: 36px;
    height: 36px;
  }
}
</style>
