/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'neon-cyan': '#00F0FF',
        'neon-magenta': '#FF0055',
        'deep-black': '#0A0A0A',
        'glass-black': 'rgba(10, 10, 10, 0.6)',
      },
      fontFamily: {
        'tech': ['"Space Grotesk"', 'sans-serif'], // On installera la police après
      }
    },
  },
  plugins: [],
}