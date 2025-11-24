/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}"
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
        display: ['Inter', 'sans-serif'],
      },
      colors: {
        // Modern Slate Palette for Neutrals
        slate: {
          50: '#f8fafc',
          100: '#f1f5f9',
          200: '#e2e8f0',
          300: '#cbd5e1',
          400: '#94a3b8',
          500: '#64748b',
          600: '#475569',
          700: '#334155',
          800: '#1e293b',
          900: '#0f172a',
        },
        // Primary Brand Color (Orange)
        primary: {
          DEFAULT: '#EA580C', // Orange 600
          50: '#fff7ed',
          100: '#ffedd5',
          200: '#fed7aa',
          300: '#fdba74',
          400: '#fb923c',
          500: '#f97316',
          600: '#ea580c',
          700: '#c2410c',
          800: '#9a3412',
          900: '#7c2d12',
        },
        // Secondary (Dark/Black)
        secondary: {
          DEFAULT: '#0F172A', // Slate 900
          50: '#f8fafc',
          100: '#f1f5f9',
          200: '#e2e8f0',
          300: '#cbd5e1',
          400: '#94a3b8',
          500: '#64748b',
          600: '#475569',
          700: '#334155',
          800: '#1e293b',
          900: '#0f172a',
        },
        // Accent (Dark Gray/Slate)
        accent: {
          DEFAULT: '#2c2f34',
          50: '#f6f7f8',
          100: '#eceef0',
          200: '#d0d5d9',
          300: '#b3b9bf',
          400: '#7a848d',
          500: '#5e6872',
          600: '#454e58',
          700: '#2c2f34',
          800: '#24272b',
          900: '#1d1f23',
        },
        // Danger (Rose)
        danger: {
          DEFAULT: '#F43F5E',
          50: '#fff1f2',
          500: '#f43f5e',
          700: '#be123c',
        },
        // Semantic Aliases
        'dark-bg': '#0f172a', // Slate 900
        'light-bg': '#f8fafc', // Slate 50
        'body-text': '#334155', // Slate 700
        'heading-text': '#1e293b', // Slate 800
      },
      boxShadow: {
        'soft': '0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03)',
        'card': '0 0 0 1px rgba(0,0,0,0.03), 0 2px 8px rgba(0,0,0,0.04)',
        'glow': '0 0 15px rgba(79, 70, 229, 0.3)',
      },
      borderRadius: {
        'xl': '1rem',
        '2xl': '1.5rem',
      }
    },
  },
  plugins: [],
}
