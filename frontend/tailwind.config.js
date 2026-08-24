/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        // 主品牌色：靛蓝紫渐变体系
        primary: {
          50: '#eef2ff',
          100: '#e0e7ff',
          200: '#c7d2fe',
          300: '#a5b4fc',
          400: '#818cf8',
          500: '#6366f1',
          600: '#4f46e5',
          700: '#4338ca',
          800: '#3730a3',
          900: '#312e81',
        },
        // 收入语义色（翡翠绿）
        income: {
          50: '#ecfdf5',
          100: '#d1fae5',
          200: '#a7f3d0',
          300: '#6ee7b7',
          400: '#34d399',
          500: '#10b981',
          600: '#059669',
          700: '#047857',
        },
        // 支出语义色（玫瑰红）
        expense: {
          50: '#fff1f2',
          100: '#ffe4e6',
          200: '#fecdd3',
          300: '#fda4af',
          400: '#fb7185',
          500: '#f43f5e',
          600: '#e11d48',
          700: '#be123c',
        },
        // 深色主题表面色
        ink: {
          900: '#070a12',
          850: '#0b0f1a',
          800: '#0f1521',
          700: '#161d2b',
          600: '#1e2738',
        },
      },
      fontFamily: {
        sans: ['Inter', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'sans-serif'],
      },
      boxShadow: {
        soft: '0 1px 3px rgba(15,23,42,0.04), 0 12px 32px -12px rgba(15,23,42,0.12)',
        'soft-lg': '0 4px 24px -6px rgba(15,23,42,0.12), 0 2px 8px -2px rgba(15,23,42,0.06)',
        glow: '0 0 0 1px rgba(99,102,241,0.18), 0 14px 40px -8px rgba(99,102,241,0.40)',
        'glow-income': '0 0 0 1px rgba(16,185,129,0.18), 0 14px 40px -10px rgba(16,185,129,0.32)',
        'glow-expense': '0 0 0 1px rgba(244,63,94,0.18), 0 14px 40px -10px rgba(244,63,94,0.30)',
      },
      backgroundImage: {
        'brand-gradient': 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #a855f7 100%)',
        'income-gradient': 'linear-gradient(135deg, #34d399 0%, #10b981 100%)',
        'expense-gradient': 'linear-gradient(135deg, #fb7185 0%, #f43f5e 100%)',
      },
      borderRadius: {
        '2xl': '1rem',
        '3xl': '1.5rem',
      },
      keyframes: {
        'fade-up': {
          '0%': { opacity: '0', transform: 'translateY(14px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        'fade-in': {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        'scale-in': {
          '0%': { opacity: '0', transform: 'scale(0.96)' },
          '100%': { opacity: '1', transform: 'scale(1)' },
        },
        float: {
          '0%,100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-12px)' },
        },
        shimmer: {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        },
      },
      animation: {
        'fade-up': 'fade-up 0.55s cubic-bezier(0.22,1,0.36,1) both',
        'fade-in': 'fade-in 0.4s ease both',
        'scale-in': 'scale-in 0.28s cubic-bezier(0.22,1,0.36,1) both',
        float: 'float 7s ease-in-out infinite',
        shimmer: 'shimmer 1.6s linear infinite',
      },
    },
  },
  plugins: [],
}
