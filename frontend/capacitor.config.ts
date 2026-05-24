import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.ttsengine.app',
  appName: 'TTS Engine',
  webDir: 'dist',
  server: {
    url: 'https://frontend-three-ivory-85.vercel.app',
    androidScheme: 'https',
  },
};

export default config;
