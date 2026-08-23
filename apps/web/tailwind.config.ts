import type { Config } from "tailwindcss";

const config: Config = {
  darkMode: ["class"],
  content: ["./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        bg: "var(--pulse-bg)",
        surface: "var(--pulse-surface)",
        "surface-2": "var(--pulse-surface-2)",
        border: "var(--pulse-border)",
        text: "var(--pulse-text)",
        "text-muted": "var(--pulse-text-muted)",
        accent: "var(--pulse-accent)",
        "accent-hover": "var(--pulse-accent-hover)",
        "accent-text": "var(--pulse-accent-text)",
        success: "var(--pulse-success)",
        danger: "var(--pulse-danger)",
        warning: "var(--pulse-warning)"
      },
      borderRadius: {
        win: "8px",
        "win-lg": "12px"
      },
      fontFamily: {
        sans: ["var(--font-sans)"],
      },
      boxShadow: {
        acrylic: "0 8px 30px rgba(0,0,0,0.12)",
        "acrylic-lg": "0 16px 48px rgba(0,0,0,0.18)"
      }
    }
  },
  plugins: []
};

export default config;
