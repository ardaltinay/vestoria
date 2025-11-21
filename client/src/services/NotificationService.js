import api from './api';

export default {
  getNotifications() {
    return api.get('/notifications', { suppressToast: true })
  },
  getUnreadCount() {
    return api.get('/notifications/unread-count', { suppressToast: true })
  },
  markAsRead(id) {
    return api.post(`/notifications/${id}/read`);
  },
  markAllAsRead() {
    return api.post('/notifications/read-all');
  }
};
