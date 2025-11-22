import api from './api';

export default {
  getActiveAnnouncements() {
    return api.get('/announcements');
  },
  createAnnouncement(announcement) {
    return api.post('/announcements/create', announcement);
  }
};
