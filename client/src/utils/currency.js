/**
 * Currency formatting utilities for Vestoria Para (VP)
 */

/**
 * Formats a number as Vestoria Para currency
 * @param {number|string} amount - The amount to format
 * @param {object} options - Formatting options
 * @param {boolean} options.showDecimals - Whether to show decimal places (default: false for whole numbers, true if decimals exist)
 * @param {boolean} options.compact - Use compact notation for large numbers (e.g., 1.5K, 2.3M)
 * @returns {string} Formatted currency string
 */
export function formatCurrency(amount, options = {}) {
  if (amount === null || amount === undefined) {
    return '0'
  }

  const num = typeof amount === 'string' ? parseFloat(amount) : amount

  if (isNaN(num)) {
    return '0'
  }

  const { showDecimals = num % 1 !== 0, compact = false } = options

  if (compact && num >= 1000) {
    return formatCompactCurrency(num, showDecimals)
  }

  const formatted = num.toLocaleString('tr-TR', {
    minimumFractionDigits: showDecimals ? 2 : 0,
    maximumFractionDigits: showDecimals ? 2 : 0,
  })

  return formatted
}

/**
 * Formats large numbers in compact notation
 * @param {number} num - The number to format
 * @param {boolean} showDecimals - Whether to show decimal places
 * @returns {string} Compact formatted string (e.g., "1.5K", "2.3M")
 */
function formatCompactCurrency(num, showDecimals) {
  const absNum = Math.abs(num)
  const sign = num < 0 ? '-' : ''

  if (absNum >= 1_000_000_000) {
    return sign + (absNum / 1_000_000_000).toFixed(showDecimals ? 1 : 0) + 'B'
  } else if (absNum >= 1_000_000) {
    return sign + (absNum / 1_000_000).toFixed(showDecimals ? 1 : 0) + 'M'
  } else if (absNum >= 1_000) {
    return sign + (absNum / 1_000).toFixed(showDecimals ? 1 : 0) + 'K'
  }

  return num.toString()
}

/**
 * Currency symbol/abbreviation for Vestoria Para
 */
export const CURRENCY_SYMBOL = 'VP'
export const CURRENCY_NAME = 'Vestoria Para'
