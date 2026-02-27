export type ThemeMode = 'system' | 'light' | 'dark'

const THEME_MODE_KEY = 'themeMode'
const DARK_CLASS = 'dark'
const SYSTEM_DARK_QUERY = '(prefers-color-scheme: dark)'

let mediaQuery: MediaQueryList | null = null

const getMediaQuery = () => {
  if (!mediaQuery) {
    mediaQuery = window.matchMedia(SYSTEM_DARK_QUERY)
  }
  return mediaQuery
}

export const getThemeMode = (): ThemeMode => {
  const mode = localStorage.getItem(THEME_MODE_KEY)
  if (mode === 'light' || mode === 'dark' || mode === 'system') {
    return mode
  }
  return 'system'
}

const resolveDark = (mode: ThemeMode) => {
  if (mode === 'dark') {
    return true
  }
  if (mode === 'light') {
    return false
  }
  return getMediaQuery().matches
}

export const applyTheme = (mode: ThemeMode = getThemeMode()) => {
  const isDark = resolveDark(mode)
  document.documentElement.classList.toggle(DARK_CLASS, isDark)
  document.documentElement.style.colorScheme = isDark ? 'dark' : 'light'
}

export const setThemeMode = (mode: ThemeMode) => {
  localStorage.setItem(THEME_MODE_KEY, mode)
  applyTheme(mode)
}

export const toggleTheme = () => {
  const mode = getThemeMode()
  const nextMode: ThemeMode = mode === 'dark' ? 'light' : 'dark'
  setThemeMode(nextMode)
}

export const watchSystemTheme = () => {
  const query = getMediaQuery()
  const onChange = () => {
    if (getThemeMode() === 'system') {
      applyTheme('system')
    }
  }
  query.addEventListener('change', onChange)
  return () => {
    query.removeEventListener('change', onChange)
  }
}
