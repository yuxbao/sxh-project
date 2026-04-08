<script setup lang="ts">
const chatStore = useChatStore();
const { activeAssistantMessageId, input, list, wsStatus, wsData } = storeToRefs(chatStore);

const latestMessage = computed(() => {
  return list.value[list.value.length - 1] ?? {};
});

const isSending = computed(() => {
  return (
    latestMessage.value?.role === 'assistant' && ['loading', 'pending'].includes(latestMessage.value?.status || '')
  );
});

const sendable = computed(
  () => (!input.value.message.trim() && !isSending.value) || ['CLOSED', 'CONNECTING'].includes(wsStatus.value)
);

function createMessageId() {
  return `msg-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
}

function getActiveAssistantMessage() {
  if (!activeAssistantMessageId.value) return null;
  return list.value.find(item => item.id === activeAssistantMessageId.value && item.role === 'assistant') ?? null;
}

watch(wsData, val => {
  if (!val) return;

  const data = JSON.parse(val);
  const assistant = getActiveAssistantMessage();
  if (!assistant) return;

  if (data.type === 'completion' && data.status === 'finished') {
    if (assistant.status !== 'error') {
      assistant.status = 'finished';
    }
    assistant.sources = Array.isArray(data.sources) ? data.sources : assistant.sources;
    activeAssistantMessageId.value = '';
    return;
  }

  if (data.error) {
    assistant.status = 'error';
    activeAssistantMessageId.value = '';
    return;
  }

  if (data.chunk) {
    assistant.status = 'loading';
    assistant.content += data.chunk;
  }
});

const handleSend = async () => {
  //  判断是否正在发送, 如果发送中，则停止ai继续响应
  if (isSending.value) {
    const { error, data } = await request<Api.Chat.Token>({ url: 'chat/websocket-token', baseURL: 'proxy-api' });
    if (error) return;

    chatStore.wsSend(JSON.stringify({ type: 'stop', _internal_cmd_token: data.cmdToken }));

    const assistant = getActiveAssistantMessage();
    if (assistant) {
      assistant.status = 'finished';
      if (!assistant.content) {
        const index = list.value.findIndex(item => item.id === assistant.id);
        if (index >= 0) list.value.splice(index, 1);
      }
    }
    activeAssistantMessageId.value = '';
    return;
  }

  const message = input.value.message.trim();
  if (!message) return;

  const now = new Date().toISOString();
  const assistantId = createMessageId();
  list.value.push({
    id: createMessageId(),
    content: message,
    role: 'user',
    status: 'finished',
    timestamp: now
  });
  chatStore.wsSend(message);
  list.value.push({
    id: assistantId,
    content: '',
    role: 'assistant',
    status: 'pending',
    timestamp: now,
    sources: []
  });
  activeAssistantMessageId.value = assistantId;
  input.value.message = '';
};

const inputRef = ref();
const isComposing = ref(false);

// 手动插入换行符（确保所有浏览器兼容）
const insertNewline = () => {
  const textarea = inputRef.value;
  const start = textarea.selectionStart;
  const end = textarea.selectionEnd;

  // 在光标位置插入换行符
  input.value.message = `${input.value.message.substring(0, start)}\n${input.value.message.substring(end)}`;

  // 更新光标位置（在插入的换行符之后）
  nextTick(() => {
    textarea.selectionStart = start + 1;
    textarea.selectionEnd = start + 1;
    textarea.focus(); // 确保保持焦点
  });
};

// ctrl + enter 换行
// enter 发送
const handShortcut = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    if (e.isComposing || isComposing.value || e.keyCode === 229) return;

    e.preventDefault();

    if (!e.shiftKey && !e.ctrlKey) {
      handleSend();
    } else insertNewline();
  }
};
</script>

<template>
  <div class="relative w-full b-1 b-#1c1c1c20 bg-#fff p-4 card-wrapper dark:bg-#1c1c1c">
    <textarea
      ref="inputRef"
      v-model="input.message"
      placeholder="给 思享汇智能助手 发送消息"
      class="min-h-10 w-full cursor-text resize-none b-none bg-transparent color-#333 caret-[rgb(var(--primary-color))] outline-none dark:color-#f1f1f1"
      @compositionstart="isComposing = true"
      @compositionend="isComposing = false"
      @keydown="handShortcut"
    />
    <div class="flex items-center justify-between pt-2">
      <div class="flex items-center text-18px color-gray-500">
        <NText class="text-14px">连接状态：</NText>
        <icon-eos-icons:loading v-if="wsStatus === 'CONNECTING'" class="color-yellow" />
        <icon-fluent:plug-connected-checkmark-20-filled v-else-if="wsStatus === 'OPEN'" class="color-green" />
        <icon-tabler:plug-connected-x v-else class="color-red" />
      </div>
      <NButton :disabled="sendable" strong circle type="primary" @click="handleSend">
        <template #icon>
          <icon-material-symbols:stop-rounded v-if="isSending" />
          <icon-guidance:send v-else />
        </template>
      </NButton>
    </div>
  </div>
</template>

<style scoped></style>
