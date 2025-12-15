import 'dart:convert';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:stomp_dart_client/stomp_dart_client.dart';

// Market WebSocket state
class MarketWebSocketState {
  final bool isConnected;
  final Map<String, dynamic>? lastUpdate;

  MarketWebSocketState({
    this.isConnected = false,
    this.lastUpdate,
  });

  MarketWebSocketState copyWith({
    bool? isConnected,
    Map<String, dynamic>? lastUpdate,
  }) {
    return MarketWebSocketState(
      isConnected: isConnected ?? this.isConnected,
      lastUpdate: lastUpdate ?? this.lastUpdate,
    );
  }
}

// Market WebSocket provider
class MarketWebSocketNotifier extends StateNotifier<MarketWebSocketState> {
  StompClient? _stompClient;
  final String serverUrl;

  MarketWebSocketNotifier({required this.serverUrl}) : super(MarketWebSocketState());

  void connect() {
    if (_stompClient != null && state.isConnected) return;

    _stompClient = StompClient(
      config: StompConfig.sockJS(
        url: serverUrl,
        onConnect: _onConnect,
        onDisconnect: _onDisconnect,
        onWebSocketError: (error) => print('WebSocket error: $error'),
        onStompError: (frame) => print('STOMP error: ${frame.body}'),
        reconnectDelay: const Duration(seconds: 5),
      ),
    );

    _stompClient!.activate();
  }

  void _onConnect(StompFrame frame) {
    print('Connected to WebSocket');
    state = state.copyWith(isConnected: true);

    // Subscribe to market updates
    _stompClient?.subscribe(
      destination: '/topic/market',
      callback: (frame) {
        if (frame.body != null) {
          try {
            final update = jsonDecode(frame.body!);
            state = state.copyWith(lastUpdate: Map<String, dynamic>.from(update));
          } catch (e) {
            print('Failed to parse market update: $e');
          }
        }
      },
    );
  }

  void _onDisconnect(StompFrame frame) {
    print('Disconnected from WebSocket');
    state = state.copyWith(isConnected: false);
  }

  void disconnect() {
    _stompClient?.deactivate();
    _stompClient = null;
    state = MarketWebSocketState();
  }

  @override
  void dispose() {
    disconnect();
    super.dispose();
  }
}

// Provider
final marketWebSocketProvider =
    StateNotifierProvider<MarketWebSocketNotifier, MarketWebSocketState>((ref) {
  // Use localhost for Android emulator, adjust for real device
  return MarketWebSocketNotifier(serverUrl: 'http://10.0.2.2:8081/ws');
});

// Stream provider for market updates
final marketUpdatesProvider = StreamProvider<Map<String, dynamic>>((ref) async* {
  final wsState = ref.watch(marketWebSocketProvider);
  if (wsState.lastUpdate != null) {
    yield wsState.lastUpdate!;
  }
});
