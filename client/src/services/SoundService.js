import { Howl } from 'howler';

class SoundService {
  constructor() {
    this.sounds = {
      notification: new Howl({ src: ['/sounds/notification.mp3'], volume: 0.5 }),
      success: new Howl({ src: ['/sounds/success.mp3'], volume: 0.5 }),
      error: new Howl({ src: ['/sounds/error.mp3'], volume: 0.5 }),
      coins: new Howl({ src: ['/sounds/coins.mp3'], volume: 0.6 }),
      click: new Howl({ src: ['/sounds/click.mp3'], volume: 0.3 }),
      hover: new Howl({ src: ['/sounds/hover.mp3'], volume: 0.1 }),
    };

    this.muted = localStorage.getItem('soundMuted') === 'true';
    this.updateMuteState();
  }

  play(soundName) {
    if (this.muted || !this.sounds[soundName]) return;
    this.sounds[soundName].play();
  }

  toggleMute() {
    this.muted = !this.muted;
    localStorage.setItem('soundMuted', this.muted);
    this.updateMuteState();
    return this.muted;
  }

  updateMuteState() {
    Howler.mute(this.muted);
  }
}

export default new SoundService();
