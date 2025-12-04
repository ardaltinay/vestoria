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
      // Add new listing to the top
      listings.value.unshift({
        id: update.id,
        item: { name: update.itemName },
        quantity: update.quantity,
        price: update.price,
        seller: { username: update.sellerName },
        isActive: true
      })
    } else if (update.type === 'BUY') {
      // Update existing listing
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
      if (authStore.user?.username === update.sellerName) {
        SoundService.play('coins')
        const { addFloatingText } = useFloatingText()
        // Add floating text at random position near center or top right
        addFloatingText(`+${formatCurrency(update.totalPrice)}`, window.innerWidth / 2, window.innerHeight / 2, { color: '#10b981' })

        // Also refresh user balance
        authStore.fetchUser()
      }
    }
  }

  const loadListings = async (page = 0, search = '') => {
    try {
      const response = await MarketService.getActiveListings({ page, search })

      // If it's the first page or a search, replace the listings
      if (page === 0) {
        listings.value = response.data.content
      } else {
        // Otherwise append for pagination
        listings.value.push(...response.data.content)
      }
      return response.data
    } catch (error) {
      console.error('Failed to load listings', error)
      throw error
    }
  }

  return {
    listings,
    isConnected,
    connect,
    disconnect,
    loadListings
  }
})
