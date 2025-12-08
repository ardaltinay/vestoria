import { defineStore } from 'pinia'
import { ref } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import MarketService from '../services/MarketService'
import SoundService from '../services/SoundService'
import { useAuthStore } from './authStore'
import { useFloatingText } from '../composables/useFloatingText'
import { formatCurrency } from '../utils/currency'

export const useMarketStore = defineStore('market', () => {
  const listings = ref([])
  const isConnected = ref(false)
  let stompClient = null

  /* Pagination State */
  const currentPage = ref(0)
  const totalPages = ref(0)
  const searchQuery = ref('')
  const pageSize = 50

  const connect = () => {
    if (isConnected.value) return

    const socket = new SockJS('http://localhost:8081/ws')
    stompClient = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        isConnected.value = true
        console.log('Connected to WebSocket')

        stompClient.subscribe('/topic/market', (message) => {
          const update = JSON.parse(message.body)
          handleMarketUpdate(update)
        })
      },
      onDisconnect: () => {
        isConnected.value = false
        console.log('Disconnected from WebSocket')
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame)
      },
      onWebSocketError: (error) => {
        console.error('WebSocket error:', error)
      }
    })

    stompClient.activate()
  }

  const disconnect = () => {
    if (stompClient) {
      stompClient.deactivate()
      stompClient = null
      isConnected.value = false
    }
  }

  const handleMarketUpdate = (update) => {
    if (update.type === 'LIST') {
      // Only add new listing if we are on the first page and NOT searching
      // This preserves the context for other pages
      if (currentPage.value === 0 && !searchQuery.value) {
        listings.value.unshift({
          id: update.id,
          item: { name: update.itemName },
          quantity: update.quantity,
          price: update.price,
          seller: { username: update.sellerName },
          isActive: true,
          qualityScore: update.qualityScore,
          itemUnit: update.itemUnit || 'PIECE'
        })

        // Optional: Remove the last item if we exceeded page size to allow strict pagination
        // But for "feed" style, letting it grow slightly is often better UX until refresh
        if (listings.value.length > pageSize) {
          listings.value.pop()
        }
      }
    } else if (update.type === 'BUY') {
      // Update existing listing wherever it is in the current view
      const listing = listings.value.find(l => l.id === update.id)
      if (listing) {
        listing.quantity -= update.quantity
        if (listing.quantity <= 0) {
          // Remove if sold out
          listings.value = listings.value.filter(l => l.id !== update.id)
        }
      }

      // Check if current user is the seller
      const authStore = useAuthStore()
      if (update.sellerName && authStore.user?.username === update.sellerName) {
        SoundService.play('coins')
        const { addFloatingText } = useFloatingText()
        addFloatingText(`+${formatCurrency(update.totalPrice)}`, window.innerWidth / 2, window.innerHeight / 2, { color: '#10b981' })

        // Also refresh user balance
        authStore.fetchUser()
      }
    } else if (update.type === 'CANCEL') {
      // Remove listing
      listings.value = listings.value.filter(l => l.id !== update.id)
    }
  }

  const loadListings = async (page = 0, search = '') => {
    try {
      // Update store state
      currentPage.value = page
      searchQuery.value = search

      const response = await MarketService.getActiveListings({ page, search })

      // If it's the first page or a search, replace the listings
      listings.value = response.data.content
      totalPages.value = response.data.totalPages

      return response.data
    } catch (error) {
      console.error('Failed to load listings', error)
      throw error
    }
  }

  return {
    listings,
    isConnected,
    currentPage,
    totalPages,
    searchQuery,
    connect,
    disconnect,
    loadListings
  }
})
