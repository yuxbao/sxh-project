/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      width: {
        navBarMDInput: '300px',
        navBarInput: '200px'
      }
    }
  },
  plugins: []
}
