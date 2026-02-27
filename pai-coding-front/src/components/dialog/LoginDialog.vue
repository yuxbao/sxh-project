<template>
  <!-- 登录 Modal -->
  <el-dialog v-model="loginModal" title="登录编程汇畅享更多权益" width="600" border="border">
    <el-divider style="margin: 10px 0"></el-divider>
    <el-container>
      <el-main>
        <el-container>
          <el-container>
            <el-main>
              <el-form ref="formRef" style="max-width: 325px" :model="dynamicValidateForm" label-width="auto"
                class="demo-dynamic">
                <el-form-item style="margin: 0">
                  <span class="bold-span mb-4">用户密码登录</span>
                </el-form-item>
                <el-form-item prop="username" label="用户名" :rules="[
                  {
                    required: true,
                    message: '用户名不能为空',
                    trigger: 'blur',
                  },
                ]">
                  <el-input v-model="dynamicValidateForm.username" placeholder="请输入用户名" />
                </el-form-item>
                <el-form-item prop="password" label="密码" :rules="[
                  {
                    required: true,
                    message: '密码不能为空',
                    trigger: 'blur',
                  },
                ]">
                  <el-input v-model="dynamicValidateForm.password" type="password" placeholder="请输入密码" />
                </el-form-item>
                <el-form-item class="center-content">
                  <el-button type="primary" @click="submitForm(formRef)">提交</el-button>
                  <el-button @click="resetForm(formRef)">清空</el-button>
                </el-form-item>
              </el-form>
            </el-main>
            <el-footer>
              <div class="other-login-box">
                <div class="oauth-box">
                  <span>其他登录——敬请期待</span>
                </div>
              </div>
            </el-footer>
          </el-container>
          <el-container>
            <el-main>
              <div class="tabpane-container"
                style="display: flex; flex-direction: column; justify-content: space-between; align-content: center">
                <span class="wx-login-span-info center-content">微信扫码/长按识别登录</span>
                <div class="first center-content">
                  <img class="signin-qrcode" width="150px" src="https://96e5ee1.webp.li/qrcode.png" />
                </div>

                <div class="explain center-content">
                  <span>
                    <bold>输入验证码</bold> <span class="link-color">{{ code }}</span>
                  </span>
                  <div><span id="state">{{ state }}</span> <a class="bold-span underline cursor-pointer link-color"
                      @click="refreshCode">手动刷新</a></div>
                </div>
              </div>
            </el-main>
          </el-container>

        </el-container>

      </el-main>
      <el-footer>
        <div class="modal-footer">
          <div class="agreement-box">
            <div class="mdnice-user-dialog-footer center-content">
              <p id="login-agreement-message">登录即同意
                <span class="font-bold"> 用户协议</span>（现在没什么协议）
                和
                <span class="font-bold"> 隐私政策</span>（现在没什么隐私）
              </p>
            </div>
          </div>
          <!--          <div class="mock-login flex flex-grow" v-if="global.env !== 'prod'">-->
          <!--            &lt;!&ndash; 非生产环境，使用模拟登陆  &ndash;&gt;-->
          <!--            <el-button @click="mockLogin2">随机新用户</el-button>-->
          <!--            <el-button @click="mockLogin2">一键登录</el-button>-->
          <!--          </div>-->
        </div>
      </el-footer>
    </el-container>

  </el-dialog>
</template>
<script setup lang="ts">

import { onBeforeUnmount, reactive, ref, watch } from 'vue'
import type { FormInstance } from 'element-plus'
import { doGet, doPost, mockLogin2XML, mockLoginXML } from '@/http/BackendRequests'
import type { CommonResponse, GlobalResponse } from '@/http/ResponseTypes/CommonResponseType'
import { BASE_URL, GLOBAL_INFO_URL, LOGIN_USER_NAME_URL } from '@/http/URL'
import { getCookie, messageTip, notifyMsg, refreshPage, setAuthToken } from '@/util/utils'
import { MESSAGE_TYPE } from '@/constants/MessageTipEnumConstant'
import { COOKIE_DEVICE_ID, COOKIE_SESSION_ID } from '@/constants/CookieConstants'
import { useGlobalStore } from '@/stores/global'
const globalStore = useGlobalStore()
const global = globalStore.global

const props = defineProps<{
  clicked: boolean,
}>()

const loginModal = ref(false)

watch(() => props.clicked, (clicked) => {
  if (!clicked) {
    return
  }
  loginModal.value = true
  reconnectAttempt = 0
  buildConnect()
})

watch(loginModal, (visible) => {
  if (!visible) {
    stopConnect()
    state.value = '有效期五分钟 👉'
  }
})

onBeforeUnmount(() => {
  stopConnect()
})

const formRef = ref<FormInstance>()

const dynamicValidateForm = reactive<{
  username: string,
  password: string
}>({
  username: '',
  password: '',
})


const submitForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.validate((valid) => {
    if (valid) {
      console.log(dynamicValidateForm)
      doPost<CommonResponse>(LOGIN_USER_NAME_URL, {
        username: dynamicValidateForm.username,
        password: dynamicValidateForm.password
      })
        .then((response) => {
          if (response.data.status.code === 0) {
            messageTip("登录成功", MESSAGE_TYPE.SUCCESS)
            setAuthToken(response.data.result.token)
            console.log(response.data)
            refreshPage()
          }
        })
        .catch((error) => {
          console.error(error)
        })
    } else {
      messageTip("请按要求填写用户名密码", MESSAGE_TYPE.ERROR)
    }
  })
}

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
}


// ==========模拟登录==========
const mockLogin = () => {
  mockLoginXML<CommonResponse>(code.value)
    .then((response) => {
      console.log(response)
      messageTip("登录成功", MESSAGE_TYPE.SUCCESS)
      refreshPage()
    })
    .catch((error) => {
      console.log(code)
      console.error(error)
    })
}

const mockLogin2 = () => {
  mockLogin2XML<CommonResponse>(code.value)
    .then((response) => {
      messageTip("登录成功", MESSAGE_TYPE.SUCCESS)
      refreshPage()
    })
    .catch((error) => {
      console.log(code)
      console.error(error)
    })
}

// ==========长连接==========
// /**
//  * 记录长连接
//  * @type {null}
//  */
const reconnectBaseDelayMs = 1000
const reconnectMaxDelayMs = 20000
const reconnectMaxAttempts = 8

let reconnectAttempt = 0
let reconnectTimer: number | null = null

let sseSource: EventSource | null = null
let intHook: number | null = null
let deviceId = ''
let handlingLoginSuccess = false
const code = ref('')
const state = ref('有效期五分钟 👉')
let fetchCodeCnt = 0

function parseCookieValue(cookiePayload: string, cookieName: string): string {
  const cookieParts = cookiePayload.split(';')
  for (const cookiePart of cookieParts) {
    const segment = cookiePart.trim()
    if (segment.startsWith(`${cookieName}=`)) {
      return segment.substring(cookieName.length + 1)
    }
  }
  return ''
}

function hasSessionCookie() {
  return document.cookie.includes(`${COOKIE_SESSION_ID}=`)
}

function persistSessionCookie(cookiePayload: string): boolean {
  const normalizedCookie = cookiePayload.trim().endsWith(';')
    ? cookiePayload.trim()
    : `${cookiePayload.trim()};`

  if (!normalizedCookie.includes('=')) {
    return false
  }

  document.cookie = normalizedCookie
  if (hasSessionCookie()) {
    return true
  }

  const sessionToken = parseCookieValue(normalizedCookie, COOKIE_SESSION_ID)
  if (!sessionToken) {
    return false
  }

  document.cookie = `${COOKIE_SESSION_ID}=${sessionToken};path=/;`
  return hasSessionCookie()
}

function wait(ms: number): Promise<void> {
  return new Promise((resolve) => {
    window.setTimeout(resolve, ms)
  })
}

async function syncLoginStatus(): Promise<boolean> {
  for (let retry = 0; retry < 3; retry += 1) {
    try {
      const response = await doGet<CommonResponse>(GLOBAL_INFO_URL, {})
      globalStore.setGlobal(response.data.global)
      if (response.data.global.isLogin) {
        return true
      }
    } catch (error) {
      console.error("同步登录态失败", error)
    }
    await wait(350)
  }
  return false
}

async function handleLoginSuccess(cookiePayload: string) {
  if (handlingLoginSuccess) {
    return
  }
  handlingLoginSuccess = true
  stopConnect()
  state.value = '登录处理中'

  const sessionToken = parseCookieValue(cookiePayload, COOKIE_SESSION_ID)
  if (sessionToken) {
    setAuthToken(sessionToken)
  }

  const cookiePersisted = persistSessionCookie(cookiePayload)
  if (!cookiePersisted) {
    state.value = '登录失败，请手动重试'
    messageTip("登录态写入失败，请刷新后重试", MESSAGE_TYPE.ERROR)
    handlingLoginSuccess = false
    return
  }

  const loginSucceed = await syncLoginStatus()
  if (loginSucceed) {
    state.value = '登录成功'
    loginModal.value = false
    messageTip("登录成功", MESSAGE_TYPE.SUCCESS)
    handlingLoginSuccess = false
    return
  }

  state.value = '登录成功，刷新页面中'
  handlingLoginSuccess = false
  refreshPage()
}

function clearFetchTimer() {
  if (intHook != null) {
    window.clearInterval(intHook)
    intHook = null
  }
}

function clearReconnectTimer() {
  if (reconnectTimer != null) {
    window.clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}

function stopConnect() {
  clearReconnectTimer()
  clearFetchTimer()
  if (sseSource != null) {
    try {
      sseSource.close()
    } catch (e) {
      console.log("关闭连接失败", e)
    }
    sseSource = null
  }
}

function scheduleReconnect() {
  if (!loginModal.value) {
    return
  }
  if (reconnectAttempt >= reconnectMaxAttempts) {
    state.value = '连接失败，请稍后手动刷新'
    messageTip("连接失败，请稍后再试", MESSAGE_TYPE.WARNING)
    return
  }

  const delay = Math.min(
    reconnectBaseDelayMs * 2 ** reconnectAttempt,
    reconnectMaxDelayMs
  )
  reconnectAttempt += 1
  clearReconnectTimer()
  reconnectTimer = window.setTimeout(() => {
    buildConnect()
  }, delay)
}

/**
 * 建立半长连接，用于实现自动登录
 */
function buildConnect() {
  stopConnect()
  if (!loginModal.value) {
    return
  }

  if (!deviceId) {
    deviceId = getCookie(COOKIE_DEVICE_ID)
    console.log("获取设备id: ", deviceId)
  }
  const subscribeUrl = `${BASE_URL}/subscribe?deviceId=${encodeURIComponent(deviceId)}`
  const source = new EventSource(subscribeUrl)
  sseSource = source

  source.onmessage = function (event) {
    const text = event.data.replaceAll("\"", "").trim()
    console.log("receive: " + text)

    let newCode = ''
    if (text.startsWith('refresh#')) {
      // 刷新验证码
      newCode = text.substring(8).trim()
      code.value = newCode
      state.value = '已刷新 '
      notifyMsg("验证码已刷新", "二维码已更新，请重新扫码", MESSAGE_TYPE.INFO)
    } else if (text === 'scan') {
      // 二维码扫描
      state.value = '已扫描 '
      notifyMsg("扫码成功", "您已成功扫码，请在手机上确认登录", MESSAGE_TYPE.SUCCESS)
      // stateTag.text("已扫描 ");
    } else if (text.startsWith('login#')) {
      // 登录格式为 login#cookie
      console.log("登录成功,保存cookie", text)
      void handleLoginSuccess(text.substring(6))
    } else if (text.startsWith("init#")) {
      newCode = text.substring(5).trim()
      code.value = newCode
      console.log("初始化验证码: ", newCode)
    }

    if (newCode) {
      clearFetchTimer()
    }
  }

  source.onopen = function (evt) {
    reconnectAttempt = 0
    deviceId = getCookie(COOKIE_DEVICE_ID)
    state.value = '连接成功'
    console.log("开始订阅, 设备id=", deviceId, evt)
  }

  source.onerror = function () {
    state.value = '连接中断，正在重连'
    stopConnect()
    scheduleReconnect()
  }

  fetchCodeCnt = 0
  intHook = window.setInterval(() => fetchCode(), 1000)
}

function fetchCode() {
  if (deviceId) {
    if (++fetchCodeCnt > 5) {
      // 为了避免不停的向后端发起请求，做一个最大的重试计数限制
      clearFetchTimer()
      return
    }

    doGet('/login/fetch?deviceId=' + deviceId, {}, 'text')
      .then((response) => {
        console.log(response)
        if (response.data) {
          if (response.data !== 'fail') {
            // @ts-ignore
            code.value = response.data
            clearFetchTimer()
          }
        }
      })
      .catch((error) => {
        console.error(error)
      })
  } else {
    console.log("deviceId未获取，稍后再试!");
  }
}

function refreshCode() {
  doGet('/login/refresh?deviceId=' + deviceId, {}, 'json')
    .then((response) => {
      // @ts-ignore
      const validationCode = response.data['result']['code']
      // @ts-ignore
      const reconnect = response.data['result']['reconnect']
      console.log("验证码刷新完成: ", response)

      if (reconnect) {
        // 重新建立连接
        reconnectAttempt = 0
        buildConnect()
        state.value = '已刷新'
      } else if (validationCode) {
        if (code.value !== validationCode) {
          console.log("主动刷新验证码!")
          code.value = validationCode
          state.value = '已刷新'
        } else {
          console.log("验证码已刷新了!")
        }
      }
    })
    .catch((error) => {
      console.error(error)
    })
}




</script>



<style scoped></style>
