import { defineConfig } from 'vite';

export default defineConfig({
  server: {
    port: 5173,
    // Keeps the browser on one origin in dev: /api goes to Spring Boot.
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
