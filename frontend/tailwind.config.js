/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        // 主品牌色：深松绿（成熟、专业、金融信任感）
        primary: {
          50: '#eff5f2',
          100: '#dce9e2',
          200: '#bbd3c7',
          300: '#92b7a4',
          400: '#6a9a84',
          500: '#4c7f6a',
          600: '#3b6a57',
          700: '#305647',
          800: '#29463b',
          900: '#223a31',
          950: '#10201a',
        },
        // 收入语义色（绿）
        income: {
          50: '#eef6f1',
          100: '#d8ebe0',
          200: '#b3d7c4',
          300: '#85bda2',
          400: '#5aa082',
          500: '#3c8567',
          600: '#2e6b53',
          700: '#265645',
        },
        // 支出语义色（赭红）
        expense: {
          50: '#fbf2f0',
          100: '#f5e2de',
          200: '#e8c2ba',
          300: '#d79d92',
          400: '#c47a6c',
          500: '#a85c4d',
          600: '#8f483c',
          700: '#753a31',
        },
        // 墨色：文字与深色表面
        ink: {
          950: '#101512',
          900: '#14181b',
          850: '#191e21',
          800: '#1f2427',
          700: '#29302f',
          600: '#37403e',
        },
      },
      fontFamily: {
        sans: ['-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'sans-serif'],
      },
      boxShadow: {
        soft: '0 1px 2px rgba(23, 32, 28, 0.04), 0 8px 24px -12px rgba(23, 32, 28, 0.10)',
        'soft-lg': '0 2px 4px rgba(23, 32, 28, 0.04), 0 16px 40px -16px rgba(23, 32, 28, 0.14)',
      },
      backgroundImage: {
        'brand-gradient': 'linear-gradient(135deg, #305647 0%, #3b6a57 100%)',
      },
      keyframes: {
        'fade-up': {
          '0%': { opacity: '0', transform: 'translateY(10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        'fade-in': {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        'scale-in': {
          '0%': { opacity: '0', transform: 'scale(0.97)' },
          '100%': { opacity: '1', transform: 'scale(1)' },
        },
      },
      animation: {
        'fade-up': 'fade-up 0.4s cubic-bezier(0.22,1,0.36,1) both',
        'fade-in': 'fade-in 0.3s ease both',
        'scale-in': 'scale-in 0.22s cubic-bezier(0.22,1,0.36,1) both',
      },
    },
  },
  plugins: [],
}
