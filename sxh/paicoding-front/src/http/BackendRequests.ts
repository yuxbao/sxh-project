import axios, { AxiosError, type AxiosResponse } from "axios";
import {clearStorage, getTokenName, messageConfirm, messageTip} from "@/util/utils";
import { BASE_URL, MOCK_LOGIN_URL } from '@/http/URL'
import { LOCALSTORAGE_AUTHORIZATION } from '@/constants/LocalStorageConstants'

axios.defaults.baseURL = BASE_URL;
axios.defaults.withCredentials = true

interface Params {
  [key: string]: any;
}

interface Data {
  [key: string]: any;
}


export function doGet<CommonResponse>(url: string, params: Params, type?: 'json'|'text'): Promise<AxiosResponse<CommonResponse>> {
  const responseType = type? type : 'json';
  return axios({
    method: 'get',
    url: url,
    params: params,
    responseType: responseType,
    headers: {
      'Content-Type': 'application/json',
    },
    withCredentials: true, // 确保发送请求时包含凭证
  });
}



export function doPost<CommonResponse>(url: string, data: Data): Promise<AxiosResponse<CommonResponse>> {
  return axios({
    method: 'post',
    url: url,
    data: data,
    headers: {
      'Content-Type': 'application/json',
    },
    withCredentials: true, // 确保发送请求时包含凭证
  });
}

export function doFilePost<T>(url: string, data: FormData): Promise<AxiosResponse<T>> {
  return axios({
    method: 'post',
    url: url,
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    withCredentials: true, // 确保发送请求时包含凭证
  });
}

export function doLoginPost<T>(url: string, data: Data): Promise<AxiosResponse<T>> {
  return axios({
    method: 'post',
    url: url,
    data: data,
    headers: {
      'Content-Type': 'application/json',
    },
  });
}

export function doPut<T>(url: string, data: Data): Promise<AxiosResponse<T>> {
  return axios({
    method: 'put',
    url: url,
    data: data,
    headers: {
      'Content-Type': 'application/json',
    },
  });
}

export function doDelete<T>(url: string, params: Params): Promise<AxiosResponse<T>> {
  return axios({
    method: 'delete',
    url: url,
    params: params,
    headers: {
      'Content-Type': 'application/json',
    },
  });
}

// 用于发送给除了springboot后端以外的其他后端
// 用于发送文件并接收文件下载
export function extraFilePostAndDownload(baseUrl: string, url: string, data: FormData, params?: Data): Promise<AxiosResponse<any>> {
  return axios({
    method: 'post',
    baseURL: baseUrl,
    url: url,
    data: data,
    params: params,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    withCredentials: false,
    responseType: 'blob', // 设置 responseType 为 'blob'
  });
}

axios.interceptors.request.use((config) => {
  let token = window.sessionStorage.getItem(getTokenName())
  if(!token){
    token = window.localStorage.getItem(getTokenName())
    // if(token){
      // config.headers['rememberMe'] = true
    // }
  }
  if(token){
    config.headers[LOCALSTORAGE_AUTHORIZATION] = token
  }
  return config;
}, (error) => {
  return Promise.reject(error);
})

axios.interceptors.response.use(
  // @ts-ignore
  function (response: AxiosResponse): AxiosResponse | undefined {
    // 2xx 范围内的状态码都会触发该函数。
    // 对响应数据做点什么
    // 拦截token验证结果，进行对应提示和页面调整
    if (response.data.code > 900) { // code 大于900说明验证未通过
      // 给前端用户提示，并且跳转页面
      messageConfirm(response.data.msg + ". 是否重新去登录？")
        .then(() => { // 用户点击确定按钮的回调
          console.log("用户点击确定");
          clearStorage();
          window.location.href = "/";
          console.log("用户点击确定2");
        })
        .catch(() => { // 用户点击取消的回调
          messageTip("取消去登录", "warning");
        });
      return; // 返回 undefined 以确保 Axios 不继续处理响应
    }
    // else if(response.data.code !== 200){
    //   messageTip(response.data.msg, "error")
    //   return;
    // }
    return response;
  },
  function (error: AxiosError): Promise<AxiosError> {
    // 超出 2xx 范围的状态码都会触发该函数。
    // 对响应错误做点什么
    return Promise.reject(error);
  }
);


// ============== 模拟登录的两个请求 ==============
export function mockLoginXML<CommonResponse>(code: string): Promise<AxiosResponse<CommonResponse>> {
  return axios({
    method: 'post',
    url: MOCK_LOGIN_URL,
    data: "<xml><URL><![CDATA[https://hhui.top]]></URL><ToUserName><![CDATA[一灰灰blog]]></ToUserName><FromUserName><![CDATA[demoUser1234]]></FromUserName><CreateTime>1655700579</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[" + code + "]]></Content><MsgId>11111111</MsgId></xml>",
    headers: {
      'Content-Type': 'application/xml',
    },
    withCredentials: true, // 确保发送请求时包含凭证
  });
}

export function mockLogin2XML<CommonResponse>(code: string): Promise<AxiosResponse<CommonResponse>> {
  const randUid = 'demoUser_' + Math.round(Math.random() * 100);
  return axios({
    method: 'post',
    url: MOCK_LOGIN_URL,
    data: "<xml><URL><![CDATA[https://hhui.top]]></URL><ToUserName><![CDATA[一灰灰blog]]></ToUserName><FromUserName><![CDATA[" + randUid + "]]></FromUserName><CreateTime>1655700579</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[" + code + "]]></Content><MsgId>11111111</MsgId></xml>",
    headers: {
      'Content-Type': 'application/xml',
    },
    withCredentials: true, // 确保发送请求时包含凭证
  });
}

// ============== Chat V2 API ==============

/**
 * 获取可用模型列表
 */
export function getChatModels<T>(): Promise<AxiosResponse<T>> {
  return doGet('/chatv2/api/models', {});
}

/**
 * 获取默认模型
 */
export function getDefaultModel<T>(): Promise<AxiosResponse<T>> {
  return doGet('/chatv2/api/models/default', {});
}

/**
 * 获取会话列表
 */
export function getConversations<T>(): Promise<AxiosResponse<T>> {
  return doGet('/chatv2/api/conversations', {});
}

/**
 * 生成新的 conversationId
 */
export function generateConversationId<T>(): Promise<AxiosResponse<T>> {
  return doPost('/chatv2/api/conversation/generate-id', {});
}

/**
 * 获取会话详情
 * 使用路径参数传递 conversationId
 */
export function getConversation<T>(conversationId: string): Promise<AxiosResponse<T>> {
  return doGet(`/chatv2/api/conversation/${conversationId}`, {});
}

/**
 * 更新会话标题
 * 使用路径参数传递 conversationId
 */
export function updateConversationTitle<T>(conversationId: string, title: string): Promise<AxiosResponse<T>> {
  return doPut(`/chatv2/api/conversation/${conversationId}/title`, { title });
}

/**
 * 删除会话
 * 使用路径参数传递 conversationId
 */
export function deleteConversation<T>(conversationId: string): Promise<AxiosResponse<T>> {
  return doDelete(`/chatv2/api/conversation/${conversationId}`, {});
}

let activeChatCancelTokenSource: ReturnType<typeof axios.CancelToken.source> | null = null

function sanitizeChatStreamText(text: string): string {
  return text
    .replaceAll('[TOOL_EXECUTING]', '')
    .replaceAll('[HEARTBEAT]', '')
    .replaceAll('[DONE]', '')
}

/**
 * 发送消息（流式响应）
 * 使用 axios onDownloadProgress 处理流式响应，参考 deepextract 实现
 * 后端直接返回纯文本流，不带 SSE "data:" 前缀
 */
export async function sendChatMessage(
  message: string,
  conversationId: string | number | null,
  modelId: string | null,
  onChunk: (chunk: string, fullText: string) => void,
  onComplete: () => void,
  onError: (error: Error) => void
): Promise<void> {
  let lastText = ''
  let requestCompleted = false
  const cancelTokenSource = axios.CancelToken.source()
  activeChatCancelTokenSource = cancelTokenSource

  const requestConfig = {
    method: 'post',
    url: '/chatv2/api/send',
    data: {
      conversationId: String(conversationId),
      modelId,
      message
    },
    headers: {
      'Content-Type': 'application/json'
    },
    responseType: 'text' as const, // 明确指定为文本类型，确保流式响应
    withCredentials: true,
    onDownloadProgress: (progressEvent: any) => {
      if (requestCompleted) return

      const responseText = progressEvent.event?.target?.responseText || ''
      const normalizedText = sanitizeChatStreamText(responseText)
      const delta = normalizedText.substring(lastText.length)

      if (responseText.includes('[DONE]')) {
        requestCompleted = true
        if (delta) {
          onChunk(delta, normalizedText)
        }
        lastText = normalizedText
        onComplete()
        activeChatCancelTokenSource = null
        cancelTokenSource.cancel('Stream completed')
        return
      }

      if (responseText.includes('[ERROR]')) {
        requestCompleted = true
        const errorMatch = responseText.match(/\[ERROR\] (.+)/)
        const errorMsg = errorMatch ? errorMatch[1] : 'Unknown error'
        activeChatCancelTokenSource = null
        onError(new Error(errorMsg))
        cancelTokenSource.cancel('Stream error')
        return
      }

      if (delta) {
        onChunk(delta, normalizedText)
        lastText = normalizedText
      }
    },
    cancelToken: cancelTokenSource.token
  }

  try {
    await axios(requestConfig)

    if (!requestCompleted) {
      onComplete()
    }
  } catch (error: any) {
    if (axios.isCancel(error)) {
      return
    }

    if (!requestCompleted) {
      onError(error instanceof Error ? error : new Error('发送消息失败'))
    }
  } finally {
    if (activeChatCancelTokenSource === cancelTokenSource) {
      activeChatCancelTokenSource = null
    }
  }
}

export function stopActiveChatMessage(): boolean {
  if (!activeChatCancelTokenSource) {
    return false
  }

  activeChatCancelTokenSource.cancel('User stopped stream')
  activeChatCancelTokenSource = null
  return true
}
