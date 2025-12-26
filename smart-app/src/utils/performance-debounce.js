/**
 * 防抖和节流工具
 * 用于优化频繁触发的操作
 */

/**
 * 防抖函数
 * 在事件被触发n秒后再执行回调，如果在这n秒内又被触发，则重新计时
 * @param {Function} func 要执行的函数
 * @param {Number} delay 延迟时间(ms)
 * @param {Boolean} immediate 是否立即执行
 * @returns {Function}
 */
export function debounce(func, delay = 300, immediate = false) {
  let timer = null

  return function (...args) {
    const context = this

    if (timer) clearTimeout(timer)

    if (immediate) {
      // 立即执行模式
      const callNow = !timer
      timer = setTimeout(() => {
        timer = null
      }, delay)
      if (callNow) func.apply(context, args)
    } else {
      // 延迟执行模式
      timer = setTimeout(() => {
        func.apply(context, args)
      }, delay)
    }
  }
}

/**
 * 节流函数
 * 规定在一个单位时间内，只能触发一次函数。如果这个单位时间内触发多次函数，只有一次生效
 * @param {Function} func 要执行的函数
 * @param {Number} delay 延迟时间(ms)
 * @param {Object} options 配置选项
 * @returns {Function}
 */
export function throttle(func, delay = 300, options = {}) {
  let timer = null
  let previous = 0

  const { leading = true, trailing = true } = options

  return function (...args) {
    const context = this
    const now = Date.now()

    if (!previous && !leading) previous = now

    const remaining = delay - (now - previous)

    if (remaining <= 0 || remaining > delay) {
      if (timer) {
        clearTimeout(timer)
        timer = null
      }
      previous = now
      func.apply(context, args)
    } else if (!timer && trailing) {
      timer = setTimeout(() => {
        previous = leading ? Date.now() : 0
        timer = null
        func.apply(context, args)
      }, remaining)
    }
  }
}

/**
 * Vue 3 防抖指令
 * 使用方法: v-debounce="handler"
 * 或: v-debounce:300="handler"
 */
export const debounceDirective = {
  mounted(el, binding) {
    const delay = Number(binding.arg) || 300
    const handler = binding.value

    if (typeof handler !== 'function') {
      console.warn('[v-debounce] 指令值必须是函数')
      return
    }

    const debouncedHandler = debounce(handler, delay)

    el._debounceHandler = debouncedHandler
    el.addEventListener('input', debouncedHandler)
    el.addEventListener('change', debouncedHandler)
  },

  unmounted(el) {
    if (el._debounceHandler) {
      el.removeEventListener('input', el._debounceHandler)
      el.removeEventListener('change', el._debounceHandler)
      delete el._debounceHandler
    }
  }
}

/**
 * Vue 3 节流指令
 * 使用方法: v-throttle="handler"
 * 或: v-throttle:300="handler"
 */
export const throttleDirective = {
  mounted(el, binding) {
    const delay = Number(binding.arg) || 300
    const handler = binding.value

    if (typeof handler !== 'function') {
      console.warn('[v-throttle] 指令值必须是函数')
      return
    }

    const throttledHandler = throttle(handler, delay)

    el._throttleHandler = throttledHandler
    el.addEventListener('click', throttledHandler)
    el.addEventListener('touchstart', throttledHandler)
  },

  unmounted(el) {
    if (el._throttleHandler) {
      el.removeEventListener('click', el._throttleHandler)
      el.removeEventListener('touchstart', el._throttleHandler)
      delete el._throttleHandler
    }
  }
}

/**
 * Vue 3 组合式函数 - 防抖
 * @param {Function} fn 要防抖的函数
 * @param {Number} delay 延迟时间
 * @returns {Function}
 */
export function useDebounce(fn, delay = 300) {
  let timer = null

  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

/**
 * Vue 3 组合式函数 - 节流
 * @param {Function} fn 要节流的函数
 * @param {Number} delay 延迟时间
 * @returns {Function}
 */
export function useThrottle(fn, delay = 300) {
  let timer = null
  let previous = 0

  return function (...args) {
    const now = Date.now()
    const remaining = delay - (now - previous)

    if (remaining <= 0) {
      if (timer) {
        clearTimeout(timer)
        timer = null
      }
      previous = now
      fn.apply(this, args)
    } else if (!timer) {
      timer = setTimeout(() => {
        previous = Date.now()
        timer = null
        fn.apply(this, args)
      }, remaining)
    }
  }
}

export default {
  debounce,
  throttle,
  debounceDirective,
  throttleDirective,
  useDebounce,
  useThrottle
}
