import { defineStore } from 'pinia'
import NotificationService from '../services/NotificationService'

export const useNotificationStore = defineStore('notifications', {
  state: () => ({
    notifications: [],
    unreadCount: 0,
    loading: false,
    error: null
  }),

  actions: {
    async fetchNotifications() {
      this.loading = true
      try {
        const [notifsRes, countRes] = await Promise.all([
          NotificationService.getNotifications(),
          NotificationService.getUnreadCount()
        ])
        this.notifications = notifsRes.data
        this.unreadCount = countRes.data
      } catch (error) {
        this.error = error
        console.error('Failed to fetch notifications', error)
      } finally {
        this.loading = false
      }
    },

    async markAllAsRead() {
      try {
        await NotificationService.markAllAsRead()
        this.unreadCount = 0
        this.notifications.forEach(n => n.isRead = true)
      } catch (error) {
        console.error('Failed to mark all read', error)
      }
    }
  }
})
