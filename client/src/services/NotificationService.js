import api from './api';

export default {
  getNotifications() {
    return api.get('/notifications');
  },
  getUnreadCount() {
    return api.get('/notifications/unread-count');
  },
  markAsRead(id) {
    return api.post(`/notifications/${id}/read`);
  },
  markAllAsRead() {
    return api.post('/notifications/read-all');
  }
};
